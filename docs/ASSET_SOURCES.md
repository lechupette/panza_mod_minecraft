# Fuentes para la renovación visual

Las texturas se preparan con fotografías reales gratuitas de Pexels. El script `tools/prepare_stock_textures.py` descarga las fotos durante la compilación, las recorta al tamaño exacto de cada textura, reduce la paleta a 32 colores y conserva la máscara alfa original cuando existe. El juego solo carga PNG ya procesados: no descarga ni transforma imágenes en tiempo de ejecución.

## Fotografías seleccionadas

| Recurso | Foto Pexels | Autor |
|---|---|---|
| Taza / agua | https://www.pexels.com/photo/white-ceramic-mug-with-coffee-4264049/ | Andrew Neel |
| Taza de café | https://www.pexels.com/photo/a-close-up-shot-of-a-cup-of-coffee-and-coffee-beans-3441194/ | Aleks Magnusson |
| Pollo crudo | https://www.pexels.com/photo/fresh-raw-chicken-meat-with-rosemary-toppings-on-white-surface-5769376/ | Leeloo The First |
| Pollo cocido | https://www.pexels.com/photo/fried-chicken-145804/ | freestocks.org |
| Bacalao | https://www.pexels.com/photo/fish-on-white-background-19993441/ | Alex Bayev |
| Salmón | https://www.pexels.com/photo/close-up-shot-of-sockeye-salmon-in-the-water-12801543/ | Timon Cornelissen |
| Pez globo | https://www.pexels.com/photo/close-up-of-a-blue-fugu-fish-5546939/ | Taryn Elliott |
| Pez tropical | https://www.pexels.com/photo/vibrant-tropical-fish-in-a-lush-aquarium-35773832/ | Nguyễn Quang Vũ |
| Planta / cerezas de café | https://www.pexels.com/photo/coffee-fruits-attached-to-the-shrubs-7125685/ | Michael Burrows |
| Etapas de la planta | https://www.pexels.com/photo/a-close-up-shot-of-a-coffea-plant-6152431/ | Sergiu Iacob |
| Café tostado | https://www.pexels.com/photo/a-close-up-shot-of-a-roasted-coffee-beans-34085/ | Negative Space |
| Café tostado alternativo | https://www.pexels.com/photo/close-up-shot-of-a-cup-of-roasted-coffee-beans-14868344/ | Emrah |
| Café molido | https://www.pexels.com/photo/close-up-shot-of-ground-coffee-14770390/ | Gergely Meszárcsek |

Licencia: https://www.pexels.com/license/  
Preguntas frecuentes: https://help.pexels.com/hc/en-us/articles/360042295174-What-is-the-license-of-the-photos-and-videos-on-Pexels

## Rendimiento y tratamiento

- Las fotos se descargan y procesan únicamente durante la compilación.
- Cada PNG final mantiene las dimensiones de la textura original y una paleta de hasta 32 colores.
- Se conserva la transparencia original para mantener las siluetas existentes.
- No hay descargas, filtros, escalados ni procesamiento de imágenes durante el juego.
- Se mantienen intactas las capas de vidrio, tapa, líquido y ojos de los frascos.
- Si Pexels no responde durante una compilación, el script conserva las texturas existentes y permite que el mod compile.

## Estado

El pipeline está integrado en GitHub Actions y también puede ejecutarse localmente con Python y Pillow. La compilación empaqueta los PNG generados dentro del JAR.
