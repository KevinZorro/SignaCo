# SignaCo — esqueleto de la app

App Android para aprender el abecedario dactilológico de la Lengua de Señas Colombiana.
Proyecto académico, Universidad Francisco de Paula Santander.

Este repositorio contiene el **esqueleto**: estructura, navegación, persistencia y pantallas
funcionando sobre datos de prueba. El reconocimiento está detrás de una interfaz con una
implementación falsa, para poder construir pantallas sin depender del modelo.

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
│   ├── senas/                   catálogo de señas (domain + data)
│   └── ui/                      tema y componentes compartidos
└── feature/
    ├── lecciones/   {domain, data, presentation/{catalogo, detalle}}
    ├── practica/    {domain, presentation}
    ├── quiz/        {domain, presentation}
    ├── progreso/    {domain, data, presentation}
    └── diccionario/ {domain, presentation}
```

Las dependencias siempre apuntan hacia `domain`. `domain` no conoce Room, ni Compose, ni
Android: son clases de Kotlin puro, y por eso se pueden probar con JUnit sin emulador.

### Dos features sin carpeta `data`

`practica` y `diccionario` no tienen capa de datos propia porque no tienen datos propios:
leen del catálogo (`core/senas`), del progreso (`feature/progreso`) y del clasificador
(`core/reconocimiento`). Cuando entre la cámara, `practica/data` será su casa natural.

### Quién depende de quién entre features

Para no crear un ciclo, las dependencias entre features van en un solo sentido:

- `lecciones.domain` → `progreso.domain` (el catálogo necesita el avance para pintarlo)
- `practica.domain` → `progreso.domain` y `lecciones.domain`
- `quiz.domain` → `progreso.domain` y `lecciones.domain`
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

- **`FakeSignClassifier`** — la que está activa. Lee los frames falsos que fabrica
  `LandmarksSimulados`, responde con un retardo parecido al real y da una confianza estable
  por letra.
- **`TFLiteSignClassifier`** — esqueleto con la firma correcta y los TODO del port. Su KDoc
  tiene los cinco pasos de normalización en orden, que es lo que no se puede equivocar.

**Cambiar de una a otra es una línea** (RNF-21), en
`core/di/ReconocimientoModule.kt`:

```kotlin
@Binds
@Singleton
abstract fun enlazarClasificador(implementacion: FakeSignClassifier): SignClassifier
//                                               ^ cambiar por TFLiteSignClassifier
```

Ni `presentation` ni `domain` se enteran.

### El andamio de simulación

Como todavía no hay cámara, la pantalla de práctica tiene cuatro botones que fuerzan cada
desenlace. Van por `SimularIntentoUseCase`, que fabrica el frame y lo pasa por
`EvaluarIntentoUseCase`: **el mismo camino que recorrerá el frame real**. Al portar la
cámara se borran tres cosas y nada más:

1. `LandmarksSimulados` (`core/reconocimiento/domain`)
2. `SimularIntentoUseCase` y `TipoDeSimulacion` (`feature/practica/domain`)
3. `ControlesDeSimulacion` dentro de `PracticaScreen`

`EvaluarIntentoUseCase` se queda igual.

## Base de datos

Room, versión 1, seis tablas: `senas`, `lecciones`, `leccion_senas`, `progreso_senas`,
`lecciones_completadas` y `racha`. Todos los DAO devuelven `Flow`.

El catálogo (27 letras y 5 lecciones) se precarga con un callback de Room en
`PrecargaDelCatalogo`. La inserción se lanza fuera de la transacción de creación, que es la
forma soportada de usar los DAO desde `onCreate`. Como todo es `Flow`, la UI recibe el
catálogo en cuanto se escribe.

Los esquemas se exportan a `app/schemas`. Al subir la versión hay que escribir la migración.

## Decisiones que conviene conocer antes de tocar algo

- **El umbral de confianza no está en el código.** Vive en `config_rechazo.json` y lo aplica
  el clasificador. Cuando `EvaluarIntentoUseCase` recibe una `LetraReconocida`, ya pasó el
  umbral. No agregar comparaciones de confianza en los casos de uso.
- **Un intento no es un frame.** `EvaluarIntentoUseCase` se llama una vez por intento del
  usuario. La estabilidad temporal (parámetro `k`) se resolverá en la capa de captura.
- **La enie está en el catálogo pero no en el MVP.** `Sena.disponible = false`: se ve en el
  diccionario, no se practica ni entra en los quiz ni en los denominadores del progreso.
- **El objetivo de un intento es el `id` de la seña, no la letra.** El `id` es la etiqueta
  que devuelve el modelo (`"A".."Z"`, `"NN"`); `letra` es lo que se le muestra al usuario y
  para la enie no coincide.
- **Sin color dinámico.** El color dinámico de Android 12+ se arma con el fondo de pantalla
  del usuario y no garantiza el contraste 4.5:1 del RNF-10. La paleta fija sí está medida.
- **Las descripciones de las señas son texto de trabajo.** Están en `CatalogoInicial.kt` y
  hay que validarlas con material de LSC antes de la entrega. La estructura no cambia.

## Privacidad

La app **no declara el permiso de INTERNET** y no debe declararlo. Todo el procesamiento es
en el dispositivo y el progreso vive solo en almacenamiento local (RNF-06 a RNF-08).

Cuando se porte la cámara habrá que agregar `android.permission.CAMERA`. Nada más.

## Accesibilidad

No es una capa de pintura al final:

- Toda señal tiene equivalente visual: los estados de práctica llevan icono, texto y color,
  nunca color solo. La vibración al acertar es opcional y está en DataStore.
- Áreas táctiles con mínimo 48×48dp.
- Barras de progreso con `contentDescription` que dice el porcentaje.
- Texto en `sp`, para que respete el escalado de fuente del sistema.

## Pruebas

Las pruebas viven en `app/src/test` y cubren la capa `domain`:

```
./gradlew testDebugUnitTest
```

Los dobles están escritos a mano en `fakes/Fakes.kt`, sin librería de mocks: son pocas
interfaces y así las pruebas se leen sin conocer una API extra.

Lo cubierto: reglas de racha, criterio de "seña dominada", registro de intentos, resumen de
progreso, evaluación de intentos con clasificador falso, generación y calificación de quiz,
armado del catálogo con progreso y búsqueda en el diccionario.

## Estado

Lo que **no** está hecho, a propósito: cámara (CameraX), MediaPipe HandLandmarker,
inferencia TFLite, imágenes o vídeos de referencia de las señas, y las extensiones
(insignias, repaso espaciado, estadísticas detalladas).
