#!/usr/bin/env python3
"""Download free Pexels stock photos and scale them with minimal visual alteration.

Photos are resized directly to the existing texture dimensions: no crop, palette
quantization, posterization, or stylized filters. Existing transparency is preserved.
If a stock URL is temporarily unavailable, its existing texture is left untouched.
"""
from __future__ import annotations

from io import BytesIO
from pathlib import Path
from urllib.request import Request, urlopen

from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / "src/main/resources/assets/ruletita/textures"

# One freely downloadable Pexels photo per visual subject. Photo IDs are stable Pexels IDs.
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


def download_photo(photo_id: int) -> bytes:
    url = (
        f"https://images.pexels.com/photos/{photo_id}/"
        f"pexels-photo-{photo_id}.jpeg?auto=compress&cs=tinysrgb&w=900"
    )
    request = Request(url, headers={"User-Agent": "PanzaMod-TexturePrep/1.0"})
    with urlopen(request, timeout=35) as response:
        return response.read()


def prepare_photo(photo_bytes: bytes, target: Path) -> None:
    with Image.open(target) as old_texture:
        old = old_texture.convert("RGBA")
    width, height = old.size

    with Image.open(BytesIO(photo_bytes)) as source:
        # Keep the complete photograph; only scale it to the existing texture size.
        photo = source.convert("RGB").resize(
            (width, height), Image.Resampling.LANCZOS
        )

    result = photo.convert("RGBA")
    alpha = old.getchannel("A")
    if alpha.getextrema() != (255, 255):
        result.putalpha(alpha)
    result.save(target, format="PNG", optimize=True)


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
                print(f"WARNING: texture target missing: {target}")
                continue
            prepare_photo(data, target)
            changed += 1
            print(f"Prepared Pexels photo {photo_id} -> {relative}")

    print(f"Prepared {changed} texture(s); {len(failed)} photo source(s) unavailable.")
    # Missing external photos must never prevent the mod itself from compiling.


if __name__ == "__main__":
    main()
