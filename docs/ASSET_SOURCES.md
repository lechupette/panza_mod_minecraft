# Fuentes para la renovación visual

Las texturas fotográficas usan imágenes gratuitas de Pexels. El proceso se ejecuta únicamente durante la compilación; el juego carga los PNG incluidos en el JAR.

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
| Café tostado | https://www.pexels.com/photo/close-up-shot-of-roasted-coffee-beans-6858480/ | Vero Andrade |
| Café tostado alternativo | https://www.pexels.com/photo/close-up-shot-of-a-cup-of-roasted-coffee-beans-14868344/ | Emrah |
| Café molido | https://www.pexels.com/photo/close-up-shot-of-ground-coffee-14770390/ | Gergely Meszárcsek |

Licencia: https://www.pexels.com/license/  
Preguntas frecuentes: https://help.pexels.com/hc/en-us/articles/360042295174-What-is-the-license-of-the-photos-and-videos-on-Pexels

## Tratamiento y compatibilidad

- Las fotografías se preparan durante la compilación, nunca dentro del juego.
- Las texturas de objetos son PNG cuadrados, opacos y autónomos. No se reutiliza la máscara alfa ni la silueta de los antiguos iconos.
- Se conserva la fotografía completa, sin recortar el sujeto; la imagen se reduce proporcionalmente a 16 × 16, se centra sobre un fondo del tono medio de la propia foto y se amplía a 64 × 64 mediante vecino más cercano (bloques de 4 × 4).
- Las etapas de la planta cultivable conservan sus sprites de bloque actuales: un recorte fotográfico opaco convertiría el cultivo en una tarjeta cuadrada y no en una planta.
- Los modelos de frascos colocados usan el atlas de bloques. El pipeline genera copias de las texturas compartidas de frascos y peces en `textures/block`; los modelos de inventario siguen usando `textures/item`.
- Si una fuente externa falla, se conserva la textura incluida en el repositorio. La compilación no descarga ni procesa imágenes durante el juego.

## Verificación de compilación

La compilación de CI prepara también las texturas del atlas de bloques antes de empaquetar el JAR. El workflow instala JDK 8 para generar los mapeos oficiales de Forge, conserva JDK 21 para compilar el mod e instala Gradle desde un espejo alternativo de la distribución oficial con reintentos.
