#!/usr/bin/env python3
"""Prepare readable, gently pixelated stock textures and block-atlas aliases.

All downloads and image processing happen during the build. The game only loads
PNG resources packaged in the mod. Original item alpha silhouettes are preserved.
"""
from __future__ import annotations

from io import BytesIO
from pathlib import Path
from shutil import copyfile
from urllib.request import Request, urlopen

from PIL import Image, ImageOps

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / "src/main/resources/assets/ruletita/textures"
OUTPUT_SIZE = 64
PIXEL_SIZE = 16
PIXEL_SCALE = 3

PHOTO_TARGETS = {
    4264049: ["item/taza.png", "item/taza_agua.png"],
    3441194: ["item/taza_agua_cafe.png", "item/taza_cafe.png"],
    5769376: ["item/pollo_carne_crudo.png"],
    145804: ["item/pollo_carne.png"],
    19993441: ["item/fish_cod.png"],
    12801543: ["item/fish_salmon.png"],
    5546939: ["item/fish_globo.png"],
    35773832: ["item/fish_tropical.png"],
    7125685: [
        "item/cerezas_cafe.png",
        "block/coffee_plant_stage1.png",
        "block/coffee_plant_stage3.png",
    ],
    6152431: ["block/coffee_plant_stage0.png", "block/coffee_plant_stage2.png"],
    6858480: ["item/cafe_tostado.png"],
    14868344: ["item/cafe_tostado2.png"],
    14770390: ["item/cafe_molido.png"],
}

# Block models use the block atlas, not the item atlas. These aliases are
# generated from the same source PNGs so both render paths resolve correctly.
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
    request = Request(url, headers={"User-Agent": "PanzaMod-TexturePrep/2.0"})
    with urlopen(request, timeout=35) as response:
        return response.read()


def prepare_photo(photo_bytes: bytes, target: Path) -> None:
    with Image.open(target) as old_texture:
        old = old_texture.convert("RGBA")

    # Preserve the authored Minecraft silhouette, but enlarge its mask with
    # nearest-neighbour so the stock image has enough pixels to remain legible.
    alpha = old.getchannel("A").resize(
        (OUTPUT_SIZE, OUTPUT_SIZE), Image.Resampling.NEAREST
    )

    with Image.open(BytesIO(photo_bytes)) as source:
        photo = source.convert("RGB")
        # Keep the complete photograph and its aspect ratio: no aggressive crop.
        photo = ImageOps.contain(
            photo, (PIXEL_SIZE, PIXEL_SIZE), Image.Resampling.LANCZOS
        )
        # Enlarge each source pixel into a crisp 3x3 block. This gives a
        # deliberate Minecraft-like pixel scale without stretching or cropping.
        photo = photo.resize(
            (photo.width * PIXEL_SCALE, photo.height * PIXEL_SCALE),
            Image.Resampling.NEAREST,
        )
        canvas = Image.new("RGBA", (OUTPUT_SIZE, OUTPUT_SIZE), (0, 0, 0, 0))
        x = (OUTPUT_SIZE - photo.width) // 2
        y = (OUTPUT_SIZE - photo.height) // 2
        canvas.alpha_composite(photo.convert("RGBA"), (x, y))
        canvas.putalpha(alpha)
        canvas.save(target, format="PNG", optimize=True)


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
            prepare_photo(data, target)
            changed += 1
            print(f"Prepared Pexels photo {photo_id} -> {relative}")

    make_block_atlas_aliases()
    print(f"Prepared {changed} photographic texture(s); {len(failed)} source(s) unavailable.")


if __name__ == "__main__":
    main()
