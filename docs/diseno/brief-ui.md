# Brief de implementación de la interfaz de SignaCO

Vas a implementar en la app de Android de SignaCO el diseño que el equipo terminó en Figma. SignaCO enseña Lengua de Señas Colombiana (LSC) a personas oyentes y reconoce las señas con la cámara mediante MediaPipe (21 puntos de la mano) y un modelo propio. El proyecto usa Kotlin, Jetpack Compose y Material 3, con Clean Architecture, Hilt y Room. Tu trabajo es la capa de presentación, así que la lógica de dominio, los datos y el reconocimiento no se tocan sin mi aprobación.

Este encargo vive en docs/diseno/brief-ui.md y CLAUDE.md lo enlaza, de modo que debes volver a leerlo al empezar cada sesión.

## De dónde sale el diseño

- El archivo de Figma es https://www.figma.com/design/SGUcf7fDUo6SdpoYOenNu5/SignaCO
- La página «SignaCO: rediseño Cuaderno» tiene la guía visual, los componentes y 10 pantallas (permiso de cámara, catálogo de lecciones, lección, cinco estados de práctica, resultado del quiz y progreso).
- Las pantallas del resto del equipo están en la misma página «SignaCO: rediseño Cuaderno», repartidas en cuatro secciones: «Diccionario», con los frames «11 Diccionario Principal» y «12 Lección de letra/gesto específico»; «Ajustes y accesibilidad», con «13 Ajustes» y «14 Tamaño y contraste»; «Lecciones», con «Lección detalle de seña», y «Primer uso y aprendizaje», con «reconocimiento_mano», que es la pantalla «Primero conoce tu mano». Los grupos y vectores sueltos de la sección «Lecciones» y los textos que acompañan a cada frame son notas del diseño, no pantallas.
- Las capturas PNG de todas las pantallas están en docs/diseno/pantallas/ y sirven de referencia aunque no haya acceso a Figma.
- Si existe docs/diseno/signaco-figma-script.js, ahí están las medidas exactas de las 10 pantallas de la página «SignaCO: rediseño Cuaderno».

El servidor de Figma ya está configurado en .mcp.json. Empieza por whoami para ver tu plan y tu tipo de puesto. Si el límite es de 6 llamadas al mes, no lo gastes explorando y trabaja con las PNG. Si no hay ese límite, usa get_metadata para el inventario de pantallas, get_variable_defs para los tokens y get_screenshot o get_design_context para cada pantalla. Cuando get_design_context te devuelva código de web (React o Tailwind), úsalo solo para leer medidas y jerarquía y escribe todo en Compose.

## Skills

La skill impeccable está en .claude/skills/impeccable y es la guía de diseño. Esto es un refinamiento y no un rediseño, de modo que se conserva la identidad de Figma (paleta, tipografía, estructura y textos). Al empezar, usa su comando init para registrar que la plataforma es Android y escribe DESIGN.md con los tokens de Figma. Durante el trabajo apóyate en sus comandos layout, typeset, quieter y distill, y al final corre audit con su guía nativa y polish. Si cualquier skill propone otra paleta, otra fuente o patrones de web, manda Figma.

## Lo que no cambia

- La estructura, el orden y el flujo de cada pantalla.
- La paleta, con Tinta #2438B8 para acciones y lo dominado, Tinta oscura #1B2459 para el texto, Tinta suave #56608A para el texto secundario, Cuadrícula #D6DEEA para líneas y divisores, Borde #BAC6DA, Resaltador #FFE14A para lo que se aprende ahora, Corrección #CF2A3A solo para errores y Papel #FFFFFF.
- La tipografía Atkinson Hyperlegible Next en 400, 700 y 800, con la escala de los estilos «SignaCO/…» de Figma (Palabra 48/800, Título 36/800, Feedback 24/800, Sección 21/700, Cuerpo 16/400, Secundario 14/400, Leyenda 12/700 y Botón 18/700).
- Si una pantalla del equipo usa un color o un tamaño que no está en estos tokens, llévalo al token más cercano y anótalo en el plan.

## Lo que sí se refina

La meta es el nivel de acabado de iOS y de HyperOS de Xiaomi, que se nota en el aire, en la alineación exacta y en que muy pocos elementos compiten por la atención. La app sigue siendo de Android, por lo que usa componentes y convenciones de Material 3 (el atrás del sistema, la navegación inferior, las áreas táctiles) y no imita controles de iOS.

- Un solo foco por pantalla. El amarillo marca únicamente lo que se aprende ahora y en áreas pequeñas, como un indicador o un subrayado, nunca como bloque grande de fondo.
- La cuadrícula del cuaderno queda solo donde significa algo, que es el cuaderno de progreso y la tarjeta de la palabra; el resto lleva fondo liso.
- Menos bordes. Agrupa con superficies tonales suaves derivadas de la paleta y con separadores finos en lugar de cajas con borde.
- Espaciado en la escala de 4 dp (4, 8, 12, 16, 24, 32 y 48), márgenes laterales de 20 dp y el mismo espacio entre elementos equivalentes en todas las pantallas.
- Jerarquía con tamaño y peso, con un máximo de tres tamaños de texto por pantalla.
- Radios consistentes, de 8 dp en piezas pequeñas y entre 16 y 24 dp en contenedores, con botones de 56 dp de alto totalmente redondeados, y sin sombras salvo en la hoja inferior de resultado.
- Íconos con el mismo trazo y el mismo tamaño (24 dp), convertidos a vector drawables.
- Movimiento corto y con propósito, entre 150 y 250 ms, con curvas de desaceleración y sin rebotes. Al acertar, el sello entra con una sola animación y una vibración corta.
- Cada componente con sus estados de presionado, deshabilitado, cargando y vacío.
- PROPUESTA, PENDIENTE DE APROBACIÓN. Estas son las piezas que hoy resaltan demasiado frente a las reglas anteriores, cada una con la forma en que debe quedar (el número es el del frame en Figma y en docs/diseno/pantallas/):
  - En el catálogo (02), la tarjeta «Saludos básicos» lleva todo el fondo en Resaltador. Debe quedar sobre Papel como las demás filas, con el amarillo solo en el cuadro de progreso de la seña en curso o en un subrayado corto, y con «Seguir» en Tinta.
  - En la barra de navegación (02, 10, 11 y 12), la pestaña activa se marca con una píldora amarilla, de modo que el amarillo señala navegación y no aprendizaje. El indicador debe ser una superficie tonal derivada de Tinta, con el ícono y la etiqueta en Tinta.
  - La ilustración del permiso (01), las miniaturas de las señas (03 y 11), la tarjeta del puntaje del quiz (09), la tarjeta de teoría (12), el área del clip de «Lección detalle de seña» y el esquema de «reconocimiento_mano» llevan cuadrícula y borde. Deben ir sobre una superficie tonal lisa y sin borde, porque la cuadrícula queda reservada al cuaderno de progreso (10) y a la tarjeta de la palabra (04 a 08).
  - Los grupos de ajustes y los selectores segmentados (13 y 14), el buscador del diccionario (11), el cuaderno de progreso (10), el chip de racha (02) y la tarjeta de la palabra (04 a 08) se separan con bordes visibles. Pasan a superficies tonales con separadores finos, los selectores usan el SegmentedButton de Material 3 sin el contorno grueso y la tarjeta de la palabra conserva su cuadrícula, pero sin borde exterior.
  - En la práctica se repite el amarillo, porque en 05 el marco punteado completo es amarillo y en 06 coinciden en ese color la píldora de estado, los puntos de la mano y la barra de coincidencia. El marco debe ir en Cuadrícula, la píldora en Papel con texto Tinta oscura y un indicador pequeño, y la barra con relleno claro y la marca del umbral; el amarillo queda en el segmento actual del progreso superior y en los puntos de la mano mientras se reconoce.
  - Sin coincidencia (08) usa Corrección en la píldora, en los puntos de la mano, en el ícono del título y en la barra. El rojo debe quedarse en el ícono del título y en el tramo de la barra, y la píldora y los puntos vuelven a neutro.
  - El botón «Detener» (05 y 06) tiene un contorno grueso del mismo ancho que el botón primario y compite con la cámara. Debe ser un botón tonal o de texto.
  - El resultado del quiz (09) junta el «3» a gran tamaño y el sello dentro de una tarjeta cuadriculada con dos botones de ancho completo debajo. El número y el sello van sobre fondo liso, sin tarjeta, y «Ver mi progreso» pasa a botón de texto para que «Repetir el quiz» sea la única acción principal.
  - En el progreso (10), «Borrar mi progreso» aparece en Corrección y con ícono a media pantalla. Debe ser un botón de texto en Tinta suave al final del contenido, y el rojo solo aparece en el diálogo de confirmación.
  - En el diccionario (11), los cuatro filtros van rellenos, «Letras» en amarillo y «Números» se sale del ancho de la pantalla. Solo el filtro seleccionado lleva relleno tonal (FilterChip de Material 3), los demás quedan neutros y sin amarillo y la fila se desplaza en horizontal.
  - Las etiquetas amarillas «Vista previa» (14) y «Practica conmigo» («Lección detalle de seña») y las cinco etiquetas de los dedos de «reconocimiento_mano» suman bloques de Resaltador que no marcan lo que se aprende ahora. Pasan a texto en Tinta suave o a una etiqueta tonal neutra, y en «reconocimiento_mano» los nombres de los dedos van como texto unido al dedo con una línea fina.

## Implementación

- Un tema propio en ui/theme que mapee el ColorScheme de Material 3 a los tokens y un CompositionLocal para los colores que Material no tiene (Resaltador, Cuadrícula y Corrección). No puede quedar ningún color ni tamaño suelto fuera del tema.
- Las fuentes van empaquetadas en res/font, porque la app funciona sin internet y no debe depender de fuentes descargables. Los tamaños de texto van en sp.
- Los componentes reutilizables van en ui/components, entre ellos el botón primario, secundario y de texto con ícono opcional, el cuadro de progreso con sus tres estados, la fila de lección, la fila de seña, la tarjeta de palabra, el marco y la píldora de estado de la cámara, el medidor de coincidencia con su umbral, la hoja de resultado, el sello y la barra de navegación.
- La barra de estado y la barra de gestos que aparecen en Figma son maquetas, así que no se dibujan. Usa enableEdgeToEdge() y WindowInsets.
- La silueta de la cámara también es de relleno. La práctica muestra la vista previa real de CameraX y, encima, el marco y los puntos de la mano dibujados con Canvas a partir de los landmarks de MediaPipe; si ya existe una capa de puntos, reestilízala.
- El contenido de Figma es de ejemplo. Las lecciones, las señas y el progreso salen de los datos reales de la app (Room y los repositorios), los textos de interfaz van a strings.xml y las miniaturas usan las imágenes de demostración que ya tiene la app.
- Los ViewModels exponen el estado de cada pantalla. Si un diseño necesita un dato nuevo, como la coincidencia en vivo o el umbral de la seña, agrégalo con el cambio mínimo y explícamelo.
- En accesibilidad, las áreas táctiles miden al menos 48 dp, los íconos y botones tienen contentDescription, el contraste cumple AA, el orden de lectura con TalkBack es correcto y el diseño aguanta la fuente del sistema al 130 % y al 200 %. El amarillo nunca es la única señal y siempre va con texto o ícono.
- Cada pantalla y cada estado de práctica (cámara lista, buscando la mano, reconociendo, acierto y sin coincidencia) tiene su @Preview con datos de ejemplo y con el peor caso, que es la palabra más larga y los nombres de lección más largos.

## Cómo trabajar

1. Primero el plan, sin escribir código, con el inventario de pantallas de Figma y el composable que les corresponde hoy, los componentes por crear, el mapeo de tokens, los cambios necesarios en los ViewModels y el orden de trabajo. Espera mi aprobación.
2. Trabaja en la rama feat/ui-cuaderno y por fases, empezando por el tema y las fuentes, siguiendo con los componentes y sus previews, después las pantallas en el orden del flujo y al final los estados de práctica.
3. Al cerrar cada fase compila con ./gradlew assembleDebug, corre lint y las pruebas que existen y haz un commit pequeño con un mensaje claro.
4. Verifica cada pantalla contra Figma. Si hay un emulador abierto, instala con ./gradlew installDebug, toma la captura con adb exec-out screencap -p, guárdala en docs/diseno/capturas/, compárala con la pantalla de Figma y corrige las diferencias en una sola ronda.
5. Para cerrar, corre audit y polish de impeccable sobre todas las pantallas y entrégame un resumen de lo que cambió respecto a Figma y por qué.

## Lo que no debes hacer

- Cambiar la paleta, la fuente o el flujo, o agregar pantallas y funciones que no estén en Figma.
- Agregar bibliotecas sin preguntarme.
- Tocar el modelo, el reconocimiento o la capa de datos sin aprobación.
