# SignaCo — esqueleto de la app

App Android para aprender el abecedario dactilológico de la Lengua de Señas Colombiana.
Proyecto académico, Universidad Francisco de Paula Santander.

El reconocimiento real ya está integrado: CameraX captura, MediaPipe extrae los landmarks de la
mano y un modelo TensorFlow Lite clasifica la seña, todo en el dispositivo.

## Cómo está organizado

Por **feature**, no por capa técnica. Cuatro personas trabajando en paralelo tocan carpetas
distintas en vez de pelearse por los mismos paquetes.

```
com.ufps.signaco
├── SignaCoApplication.kt        @HiltAndroidApp
├── MainActivity.kt              única Activity, todo lo demás es Compose
├── core/                        lo compartido entre features
│   ├── comun/                   calificadores de dispatchers y Reloj (para probar la racha)
│   ├── database/                Room: entidades, DAO, base y precarga del catálogo
│   ├── di/                      módulos de Hilt
│   ├── navegacion/              rutas type-safe, NavHost y armazón con barra inferior
│   ├── preferencias/            DataStore (domain + data)
│   ├── reconocimiento/          SignClassifier y sus dos implementaciones
│   ├── senas/                   catálogo de señas e imágenes de referencia (domain + data)
│   └── ui/                      tema y componentes compartidos
└── feature/
    ├── lecciones/   {domain, data, presentation/{catalogo, detalle}}
    ├── practica/    {domain, data/camara, presentation/camara}
    ├── quiz/        {domain, presentation}   quiz con imagen (parte 1) y con cámara (parte 2)
    ├── progreso/    {domain, data, presentation}
    └── diccionario/ {domain, presentation}
```

Las dependencias siempre apuntan hacia `domain`. `domain` no conoce Room, ni Compose, ni
Android: son clases de Kotlin puro, y por eso se pueden probar con JUnit sin emulador.

### `diccionario` sin carpeta `data`

`diccionario` no tiene capa de datos propia porque no tiene datos propios: lee del catálogo
(`core/senas`) y del progreso (`feature/progreso`). `practica` sí la tiene desde que entró la
cámara, y contiene solo `data/camara`: el resto lo toma del catálogo, del progreso y del
clasificador (`core/reconocimiento`).

### Quién depende de quién entre features

Para no crear un ciclo, las dependencias entre features van en un solo sentido:

- `lecciones.domain` → `progreso.domain` (el catálogo necesita el avance para pintarlo)
- `practica.domain` → `progreso.domain` y `lecciones.domain`
- `quiz.domain` → `progreso.domain` y `lecciones.domain`
- `quiz.presentation` → `practica.domain` (el mismo reconocimiento y la cuenta regresiva) y
  `practica.presentation.camara` (el visor de cámara compartido)
- `progreso.domain` → solo `core`

Por eso el resumen de progreso muestra el número de lecciones completadas sin denominador:
pedirle el total a `lecciones` cerraría el ciclo. Si algún día se separa en módulos de
Gradle, este es el punto que hay que revisar primero.

## La pieza clave: SignClassifier

```kotlin
interface SignClassifier {
    suspend fun clasificar(landmarks: FloatArray): ResultadoClasificacion
}
```

`ResultadoClasificacion` distingue tres casos, no dos: `LetraReconocida` (con confianza),
`SenaNoReconocida` (hay mano, la pose no es ninguna letra) y `ManoNoDetectada`.

Dos implementaciones en `core/reconocimiento/data`:

- **`TFLiteSignClassifier`** — la que está activa. Carga el `.tflite` una sola vez con
  `MappedByteBuffer`, corre la inferencia fuera del hilo principal y decide la letra de un frame.
  No guarda estado entre frames.
- **`FakeSignClassifier`** — se conserva. Devuelve resultados predecibles y sirve para las
  pruebas unitarias y para desarrollar sin dispositivo.

**Cambiar de una a otra es una línea** (RNF-21), en
`core/di/ReconocimientoModule.kt`:

```kotlin
@Binds
@Singleton
abstract fun enlazarClasificador(implementacion: TFLiteSignClassifier): SignClassifier
//                                               ^ cambiar por FakeSignClassifier para
//                                                 trabajar sin dispositivo
```

Ni `presentation` ni `domain` se enteran.

### El camino de un frame

```
CameraX ImageAnalysis
  → AnalizadorDeManosMediaPipe   (data)  landmarks crudos + handedness
  → NormalizadorDeLandmarks      (domain) los 5 pasos -> 63 floats
  → PracticaViewModel            (presentation) descarta el frame si hay otro en curso
  → ProcesarFrameUseCase         (domain)
      → SignClassifier           una letra, o rechazo
      → EstabilizadorDePredicciones  k frames iguales (RF07)
      → EvaluarIntentoUseCase    solo al confirmarse: compara y registra progreso
```

MediaPipe y CameraX no pasan de la capa de datos y de la vista previa. El dominio solo ve
`FloatArray`. No se almacena ni un frame: se analiza y se descarta (RNF-06).

**Un intento no es un frame.** La cámara da hasta 30 frames por segundo pero
`EvaluarIntentoUseCase` se llama una vez por intento, porque registra progreso. Quien decide que
un conjunto de frames constituye un intento es el estabilizador, que además confirma **una sola
vez por racha**: sin ese pestillo, sostener la mano quieta registraría diez intentos por segundo.

### Vocabulario activo: 19 de 27

El modelo clasifica 28 clases, pero la app solo enseña 19 letras:

```
A B D F G I J L O P Q R S T U V W X Y
```

Quedan fuera **C, E, H, K, M, N, Ñ y Z**. Z y Ñ son señas dinámicas y el clasificador es de poses
estáticas; las otras seis se confunden de forma sistemática con cámara real.

El modelo no se reentrena ni se modifica: el filtrado es de la app y vive en `VocabularioActivo`,
que es además la fuente de la que `CatalogoInicial` deriva el `disponible` de cada seña, para que
el catálogo y el clasificador no puedan separarse.

**Regla de decisión del modelo T-08.** Desde que la app usa `signaco_mlp_t08.tflite`, el
clasificador aplica la misma regla con la que se midió el modelo: `argmax` sobre las 28 clases,
si gana la clase de rechazo se rechaza y, si no, se acepta solo cuando la probabilidad cruda
alcanza el umbral de esa letra. Ya no restringe el `argmax` a las 19 activas: qué letras se
enseñan lo decide el catálogo (`VocabularioActivo`), y así el deletreo (`feature/deletreo`) puede
ver C, E, K, M y N, que la práctica no enseña todavía.

### Modo de depuración

`RegistroDeDepuracion` vuelca por Logcat, con la etiqueta `SignaCO_Debug` y solo en builds de
depuración: los 63 floats de entrada con sus estadísticos por eje, las 28 probabilidades **con su
suma** (si no da ~1.0, la salida no es un softmax y los umbrales no significan lo que parecen),
el top-5, el ganador global y el ganador tras filtrar, el umbral aplicado con su procedencia, y
los tiempos de MediaPipe y de la inferencia por separado.

Cierra con un diagnóstico que distingue los casos que se confunden entre sí: la inferencia no
corrió, la distribución es casi uniforme (normalización mal), ganó el rechazo, ganó una letra
excluida, o el umbral rechazó. Es lo que permite comparar contra el notebook de Python.

Los niveles están repartidos a propósito: el bloque de arranque, el diagnóstico y el latido van
en **INFO**; el volcado por frame de los 63 floats y las 28 probabilidades va en **DEBUG**. Así
`-s SignaCO_Debug:I` deja lo que se lee y quita lo que satura Logcat:

```
adb logcat -c && adb logcat -s SignaCO_Debug:I
```

Si ahí no sale nada, el siguiente paso es mirar todo lo que escribe el proceso, incluidos los
errores nativos de MediaPipe y de TensorFlow Lite, que no llevan nuestra etiqueta:

```
adb logcat -c && adb logcat --pid=$(adb shell pidof -s com.ufps.signaco)
```

### El latido, para que el silencio no sea ambiguo

Un frame sin mano no llega al clasificador, así que antes no registraba nada: "MediaPipe no ve
la mano" y "el log no funciona" se veían exactamente igual. `registrarFrameDeCamara` imprime una
línea por segundo desde el analizador, en cada frame que entrega la cámara:

```
[LATIDO] 28.6 frames/s de cámara | imagen 480x640 rot=90 | MediaPipe: 0 con mano, 28 sin mano  <- MediaPipe no ve ninguna mano
```

Late aunque MediaPipe deje de invocar su listener, porque se alimenta del frame de CameraX y no
del resultado. Si con la captura encendida no aparece ni un latido, el problema está antes de
MediaPipe: build de release, captura apagada, permiso denegado o la cámara sin enlazar.

## Base de datos

Room, versión 3, siete tablas: `senas`, `lecciones`, `leccion_senas`, `progreso_senas`,
`lecciones_completadas`, `racha` y `actividad_diaria`. Todos los DAO devuelven `Flow`.

Migraciones, en `core/database/Migraciones.kt`, probadas con `MigrationTestHelper`:

- **1 → 2:** crea `actividad_diaria` para los días de la semana de la racha.
- **2 → 3:** quita la columna `descripcion` de `senas`. Como SQLite no tiene `DROP COLUMN` antes
  de Android 14, recrea la tabla. `progreso_senas` y `leccion_senas` se respaldan antes en tablas
  temporales y se restauran al final, porque su `ON DELETE CASCADE` borraría los intentos del
  usuario si las claves foráneas estuvieran activas.

El catálogo (27 letras y 5 lecciones) se precarga con un callback de Room en
`PrecargaDelCatalogo`. La inserción se lanza fuera de la transacción de creación, que es la
forma soportada de usar los DAO desde `onCreate`. Como todo es `Flow`, la UI recibe el
catálogo en cuanto se escribe.

Los esquemas se exportan a `app/schemas`. Al subir la versión hay que escribir la migración.

## Decisiones que conviene conocer antes de tocar algo

- **La normalización no se toca.** Los cinco pasos de `NormalizadorDeLandmarks` están portados
  del spike y validados contra el notebook. Si difieren aunque sea en el orden, el modelo
  devuelve basura sin lanzar ninguna excepción. Hay pruebas unitarias que fijan cada paso.
- **Hay dos espejados distintos y ninguno sustituye al otro.** Uno voltea el bitmap de la cámara
  frontal porque MediaPipe da por supuesta una imagen de selfie; el otro voltea la X cuando la
  mano es izquierda. Si las letras salen cambiadas, son los dos sospechosos, y el log imprime
  ambos en cada frame.
- **El umbral de confianza y la k no están en el código.** Viven en `config_rechazo.json` y se
  ajustan sin recompilar. No agregar comparaciones de confianza en los casos de uso.
- **Las señas excluidas siguen en el catálogo.** `disponible = false`: se ven en el diccionario,
  no se practican ni entran en los quiz ni en los denominadores del progreso.
- **El objetivo de un intento es el `id` de la seña, no la letra.** El `id` es la etiqueta
  que devuelve el modelo (`"A".."Z"`, `"NN"`); `letra` es lo que se le muestra al usuario y
  para la enie no coincide.
- **Sin color dinámico.** El color dinámico de Android 12+ se arma con el fondo de pantalla
  del usuario y no garantiza el contraste 4.5:1 del RNF-10. La paleta fija sí está medida.
- **Las señas no tienen descripción de texto; tienen imagen.** Las descripciones se quitaron
  porque nunca se validaron con material de LSC (RF02, ERS v2.1). Las imágenes están en
  `res/drawable-nodpi/sena_<letra>.webp` y se asocian por letra en `ReferenciasEnRecursos.kt`,
  sin campo en la base. Una letra sin imagen muestra la letra grande y no entra en el quiz con
  imagen. Fuentes en `docs/fuentes_senas.md`.
- **El quiz tiene dos partes y aprueba la de cámara.** La parte con imagen no registra intentos;
  la parte con cámara sí, y es la que aprueba la lección (70 %). Diseño en
  `docs/diseno/quiz-de-la-leccion.md`.
- **Cada intento empieza solo tras una cuenta regresiva de 3 segundos** (`CuentaRegresivaUseCase`),
  en la práctica y en el quiz con cámara. «Iniciar intento» queda de respaldo.

## Privacidad

La app **no declara el permiso de INTERNET** y no debe declararlo. Declara `CAMERA` y nada más.
Todo el procesamiento es en el dispositivo y el progreso vive solo en almacenamiento local
(RNF-06 a RNF-08).

No se almacena vídeo ni imágenes: cada frame se convierte en 63 floats, se clasifica y se
descarta. El permiso se pide con una explicación de para qué sirve, y si se deniega la pantalla
ofrece reintentar o abrir los ajustes, sin cerrarse (RNF-13).

## Accesibilidad

No es una capa de pintura al final:

- Toda señal tiene equivalente visual: los estados de práctica llevan icono, texto y color,
  nunca color solo. La vibración al acertar es opcional y está en DataStore.
- Áreas táctiles con mínimo 48×48dp.
- Barras de progreso con `contentDescription` que dice el porcentaje.
- Texto en `sp`, para que respete el escalado de fuente del sistema.

## Pruebas

```
./gradlew testDebugUnitTest                # rápidas, en la JVM
./gradlew connectedDebugAndroidTest        # base de datos y Compose, necesita emulador o dispositivo
./gradlew coberturaDomain -Pcobertura      # cobertura de domain (RNF19)
```

La cobertura queda en `app/build/reports/jacoco/coberturaDomain/html/index.html`. Sin
`-Pcobertura` el agente de JaCoCo no se activa, así que el build y el CI no cambian. Última
medición: 93,8 % de las líneas de domain.

**En `app/src/test` (JVM, sin emulador):**

- Casos de uso de `domain`: reglas de racha, criterio de "seña dominada", registro de
  intentos, resumen de progreso, evaluación de intentos con clasificador falso, generación
  y calificación de los dos quizzes (imagen y cámara: letras al azar sin repetir seguidas,
  intentos por pregunta, aprobación), paso tras el acierto en la práctica, cuenta regresiva,
  imágenes de referencia y casos sin imagen, armado del catálogo con progreso y búsqueda en el
  diccionario.
- Datos de precarga: que el catálogo tenga 27 letras sin repetir, que sus etiquetas sean las
  que el modelo conoce, que cada seña practicable esté en exactamente una lección y que la
  enie no esté en ninguna.
- ViewModel: que publiquen el estado correcto y que un fallo del repositorio termine en
  `hayError`, no en una carga infinita.

**En `app/src/androidTest` (SQLite de verdad y Compose):**

- Base de datos: que la precarga corra al crear la base, que la relación lección-señas devuelva
  lo esperado, que registrar intentos acumule y que la racha y las lecciones completadas no se
  dupliquen.
- Migraciones 1 → 2 y 2 → 3: que conserven intentos, lecciones aprobadas, racha y días
  activos, también con las claves foráneas activas.
- Repositorios de señas y lecciones con sus imágenes de referencia.
- Compose: la lección carga y arranca por la primera letra; el quiz con imagen de punta a
  punta (ViewModel, repositorios y Room reales, sin Hilt de pruebas); la pantalla del quiz con
  cámara; la cuenta regresiva y el botón tras el acierto en la práctica.

El CI corre las de la JVM en cada pull request; las instrumentadas, en el push a `master` o a
mano desde *Actions → CI → Run workflow* eligiendo la rama.

Los dobles están escritos a mano en `fakes/Fakes.kt`, sin librería de mocks: son pocas
interfaces y así las pruebas se leen sin conocer una API extra.

## El build: por qué está configurado así

Esta configuración es poco habitual y cada pieza tiene un motivo. Antes de "arreglar"
cualquiera de estas cosas, leer esto: todas salieron de fallos reales de CI.

**No se aplica `org.jetbrains.kotlin.android`.** AGP 9 trae su propio Kotlin integrado.
Si además se aplica el plugin de Kotlin, el build revienta al configurarse:

```
ClassCastException: ApplicationExtensionImpl cannot be cast to
                    com.android.build.gradle.BaseExtension
```

Pasa igual con Kotlin 2.2.10 y con 2.4.20. Los plugins de compilador que sí se aplican
(Compose, serialization) y KSP funcionan sobre el Kotlin integrado sin problema.

**`android.disallowKotlinSourceSets=false` en gradle.properties.** KSP registra sus
carpetas generadas por el DSL `kotlin.sourceSets`, que el Kotlin integrado rechaza por
defecto. La bandera es la vía de escape que documenta el propio AGP. Se puede quitar
cuando KSP pase a usar `android.sourceSets`.

**Hilt 2.60.1 como mínimo.** Las versiones anteriores (2.57.x) fallan al aplicarse sobre
AGP 9, en `HiltGradlePlugin.apply`.

**Los iconos de Material llevan versión explícita (1.7.8).** El BOM de Compose ya no los
gestiona y `material3` dejó de arrastrarlos, así que sin la dependencia explícita las
pantallas no compilan (`Unresolved reference 'Icons'`).

## Integración continua

`.github/workflows/ci.yml`, **una sola verificación por cambio**:

| Evento | `verificacion` | `pruebas-instrumentadas` |
|---|---|---|
| `pull_request` | sí | no |
| `push` a `master` | sí | sí |
| `workflow_dispatch` | sí | sí |

| Job | Qué hace |
|---|---|
| `verificacion` | `assembleDebug`, `testDebugUnitTest` y `lintDebug`, en una sola invocación de Gradle |
| `pruebas-instrumentadas` | las pruebas de base de datos en un emulador API 34 |

El emulador es lo más lento del pipeline y lo que comprueba —que Room ejecute sus consultas
contra un SQLite real— casi no cambia entre commits de una rama, así que solo corre al llegar
a `master`.

**Para sincronizar tu rama con `master` no abras un pull request de `master` hacia la rama**:
eso dispara una verificación extra sobre código ya verificado. Usa el botón *Update branch* del
propio pull request, o en local `git merge master`.

`assembleDebug` es más que una compilación: Room valida cada `@Query` contra el esquema y
Hilt valida el grafo completo de dependencias. Si una consulta tiene mal un nombre de columna
o falta un `@Binds`, el job falla ahí.

Los reportes de pruebas y de lint quedan como artefactos de cada ejecución.

## Estado

El estado de cada requerimiento funcional (dónde está, qué prueba lo cubre y qué falta) está en
`docs/trazabilidad_rf.md`. En resumen, lo que **no** está hecho:

- **RF19:** registrar y mostrar las categorías completadas (las lecciones sí se registran).
- **RF14 y RF26:** el criterio de «dominada» y el ajuste del umbral difieren del ERS. Están
  pendientes de decisión del equipo (sección 8 del ERS).
- Las extensiones: insignias (RF22), los otros formatos de quiz (RF17), estadísticas (RF24),
  repaso espaciado (RF25) y tema oscuro (RF27).

Pendiente de medir en dispositivo: los fps del RNF02, la latencia del RNF01, la exactitud con
cámara real del RNF04 y la rotación del RNF14.

