#!/usr/bin/env python3
"""Build opaque, square, pixelated photo textures and jar atlas aliases.

Photos are complete standalone PNG textures: no old item silhouette mask is
reused. Processing runs only in CI/build time; Minecraft loads packaged PNGs.
"""
from __future__ import annotations

from io import BytesIO
from pathlib import Path
from shutil import copyfile
from urllib.request import Request, urlopen

from PIL import Image, ImageStat

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / "src/main/resources/assets/ruletita/textures"
OUTPUT_SIZE = 64  # solo texturas de modelo (peces)
PIXEL_SIZE = 16

PHOTO_TARGETS = {
    4264049: ["item/taza.png", "item/taza_agua.png"],
    3441194: ["item/taza_agua_cafe.png", "item/taza_cafe.png"],
    5769376: ["item/pollo_carne_crudo.png"],
    145804: ["item/pollo_carne.png"],
    19993441: ["item/fish_cod.png"],
    12801543: ["item/fish_salmon.png"],
    5546939: ["item/fish_globo.png"],
    35773832: ["item/fish_tropical.png"],
    7125685: ["item/cerezas_cafe.png"],
    6858480: ["item/cafe_tostado.png"],
    14868344: ["item/cafe_tostado2.png"],
    14770390: ["item/cafe_molido.png"],
}

# Block models use the block atlas, not the item atlas. These aliases copy
# the finished standalone source PNGs so placed jars resolve the same artwork.
BLOCK_ATLAS_ALIASES = (
    "jar_glass.png",
    "jar_lid.png",
    "jar_liquid.png",
    "fish_eye.png",
    "fish_cod.png",
    "fish_salmon.png",
    "fish_tropical.png",
    "fish_globo.png",
)


def download_photo(photo_id: int) -> bytes:
    url = (
        f"https://images.pexels.com/photos/{photo_id}/"
        f"pexels-photo-{photo_id}.jpeg?auto=compress&cs=tinysrgb&w=1200"
    )
    request = Request(url, headers={"User-Agent": "PanzaMod-TexturePrep/3.0"})
    with urlopen(request, timeout=35) as response:
        return response.read()


FISH_TARGETS = {"item/fish_cod.png", "item/fish_salmon.png", "item/fish_globo.png", "item/fish_tropical.png"}


def _remove_background(img: Image.Image, work: int = 160, tolerance: int = 48) -> Image.Image:
    """Return RGBA where the border-connected background (flood fill) is transparent."""
    from collections import deque
    small = img.resize((work, work), Image.Resampling.BILINEAR)
    px = small.load()
    border = [px[x, 0] for x in range(work)] + [px[x, work - 1] for x in range(work)] \
        + [px[0, y] for y in range(work)] + [px[work - 1, y] for y in range(work)]
    bg = tuple(sorted(c[i] for c in border)[len(border) // 2] for i in range(3))

    def close(c):
        return sum(abs(c[i] - bg[i]) for i in range(3)) <= tolerance * 3 // 2

    seen = [[False] * work for _ in range(work)]
    queue = deque()
    for x in range(work):
        queue.extend(((x, 0), (x, work - 1)))
    for y in range(work):
        queue.extend(((0, y), (work - 1, y)))
    while queue:
        x, y = queue.popleft()
        if not (0 <= x < work and 0 <= y < work) or seen[y][x] or not close(px[x, y]):
            continue
        seen[y][x] = True
        queue.extend(((x + 1, y), (x - 1, y), (x, y + 1), (x, y - 1)))
    mask = Image.new("L", (work, work), 255)
    mp = mask.load()
    for y in range(work):
        for x in range(work):
            if seen[y][x]:
                mp[x, y] = 0
    mask = mask.resize(img.size, Image.Resampling.BILINEAR)
    out = img.convert("RGBA")
    out.putalpha(mask.point(lambda v: 255 if v > 127 else 0))
    return out


def prepare_photo(photo_bytes: bytes, target: Path, relative: str = "") -> None:
    with Image.open(photo_bytes if hasattr(photo_bytes, "read") else BytesIO(photo_bytes)) as source:
        photo = source.convert("RGB")
        if relative in FISH_TARGETS:
            # Model textures: keep opaque, centered crop, light pixelation (details stay readable).
            side = min(photo.size)
            left, top = (photo.width - side) // 2, (photo.height - side) // 2
            photo = photo.crop((left, top, left + side, top + side))
            photo = photo.resize((PIXEL_SIZE, PIXEL_SIZE), Image.Resampling.LANCZOS)
            photo.resize((OUTPUT_SIZE, OUTPUT_SIZE), Image.Resampling.NEAREST).save(target, format="PNG", optimize=True)
            return
        # Inventory items: plain PNG with transparent background, subject cropped and centred.
        rgba = _remove_background(photo)
        box = rgba.getchannel("A").getbbox()
        if box:
            rgba = rgba.crop(box)
        side = max(rgba.size)
        square = Image.new("RGBA", (side, side), (0, 0, 0, 0))
        square.paste(rgba, ((side - rgba.width) // 2, (side - rgba.height) // 2))
        inner = PIXEL_SIZE - 2  # 1 px margin like vanilla items
        rgb = square.convert("RGB").resize((inner, inner), Image.Resampling.LANCZOS)
        alpha = square.getchannel("A").resize((inner, inner), Image.Resampling.BOX).point(lambda v: 255 if v > 127 else 0)
        rgb.putalpha(alpha)
        final = Image.new("RGBA", (PIXEL_SIZE, PIXEL_SIZE), (0, 0, 0, 0))
        final.paste(rgb, (1, 1))
        final.save(target, format="PNG", optimize=True)


def make_block_atlas_aliases() -> None:
    item_dir = ASSETS / "item"
    block_dir = ASSETS / "block"
    block_dir.mkdir(parents=True, exist_ok=True)
    for filename in BLOCK_ATLAS_ALIASES:
        source = item_dir / filename
        if not source.is_file():
            raise FileNotFoundError(f"Required jar texture is missing: {source}")
        copyfile(source, block_dir / filename)
        print(f"Prepared block-atlas texture alias: {filename}")


def main() -> None:
    changed = 0
    failed = []
    for photo_id, targets in PHOTO_TARGETS.items():
        try:
            data = download_photo(photo_id)
        except Exception as error:
            failed.append((photo_id, str(error)))
            print(f"WARNING: Pexels photo {photo_id} unavailable; keeping current texture ({error})")
            continue

        for relative in targets:
            target = ASSETS / relative
            if not target.is_file():
                raise FileNotFoundError(f"Texture target missing: {target}")
            prepare_photo(data, target, relative)
            changed += 1
            print(f"Prepared Pexels photo {photo_id} -> {relative}")

    make_block_atlas_aliases()
    print(f"Prepared {changed} standalone photographic texture(s); {len(failed)} source(s) unavailable.")


if __name__ == "__main__":
    main()
