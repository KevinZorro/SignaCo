# SignaCO — Contexto del proyecto

App Android para aprender el abecedario de la Lengua de Señas Colombiana (LSC). Proyecto académico, Universidad Francisco de Paula Santander, Ingeniería de Sistemas. Equipo de 4 personas, 14 semanas.

## Qué es la app

Un solo usuario aprende LSC: abre una lección, ve las señas de referencia, las practica frente a la cámara con retroalimentación inmediata (acertada / no acertada), hace un quiz al final, y su progreso se guarda en el dispositivo. Funciona sin conexión a internet.

**No es** una app de traducción ni de comunicación entre dos personas. Ese fue un enfoque anterior que se descartó por inviable. Si una propuesta empieza a parecerse a "traducir una conversación en tiempo real", está fuera de alcance.

Dirigida por igual a personas oyentes que quieren aprender LSC y a personas sordas que quieren reforzarla.

## Stack (no negociable — viene del ERS)

| Área | Tecnología |
|---|---|
| Lenguaje | Kotlin, minSdk 26 (Android 8.0) |
| UI | Jetpack Compose + Material 3 |
| Arquitectura | Clean Architecture: presentation / domain / data |
| Patrón UI | MVVM, estado con StateFlow |
| DI | Hilt |
| Asincronía | Coroutines + Flow (nunca callbacks manuales) |
| Persistencia | Room (datos), DataStore (preferencias) |
| Navegación | Navigation Compose |
| Cámara | CameraX |
| Visión | MediaPipe Tasks (HandLandmarker) |
| Inferencia | TensorFlow Lite |

Estas son restricciones de diseño del ERS (RD-01 a RD-03), no preferencias. No las cambies ni sugieras alternativas.

## Organización del código

Por **feature**, no por capa técnica:

```
feature/
  lecciones/{presentation,domain,data}
  practica/{presentation,domain,data}
  quiz/{presentation,domain,data}
  progreso/{presentation,domain,data}
  diccionario/{presentation,domain,data}
core/
  database/  ui/  di/  reconocimiento/
```

Razón: cuatro personas trabajando en paralelo. Organizar por capa haría que todos editaran los mismos paquetes y se pisaran constantemente.

Dependencias siempre **hacia domain**. `domain` no conoce ni a `data` ni a `presentation`, y no importa nada de Android.

## La pieza central: SignClassifier

```kotlin
interface SignClassifier {
    suspend fun clasificar(landmarks: FloatArray): ResultadoClasificacion
}
```

Dos implementaciones en `data`:
- `FakeSignClassifier` — resultados simulados, para desarrollar UI sin el modelo
- `TFLiteSignClassifier` — la real

Cambiar entre ellas es **una línea en el módulo de Hilt**. Esto es el RNF-21 del ERS y no es opcional: el modelo debe ser reemplazable sin tocar presentation ni domain.

`ResultadoClasificacion` distingue tres casos, no dos: letra reconocida (con confianza), seña no reconocida, y mano no detectada. Son tres estados distintos en la UI.

## El modelo de reconocimiento (ya resuelto)

No hay que entrenarlo ni modificarlo. Estado:

- `signaco_mlp_t08.tflite` — 193 KB, float32 sin cuantizar, 28 clases (27 letras + `none`), umbral por letra en `config_rechazo.json` (reemplazó al de 59 KB, decisión de Kevin, 2026-11-21)
- `config_rechazo.json` — etiquetas, umbrales, parámetro `k` de estabilidad temporal
- `hand_landmarker.task` — 7.46 MB, detector de MediaPipe
- Total desplegado: ~7.65 MB de los 20 MB que permite RNF-03

Exactitud: 88.8 % por seña, con evaluación persona-independiente.

### Normalización de landmarks — punto crítico

El modelo recibe **63 floats**, no imágenes. La normalización debe ser exactamente esta, en este orden:

1. Restar a todos los puntos la posición del landmark 0 (la muñeca)
2. Calcular la escala como la norma euclidiana máxima entre los puntos ya trasladados
3. Dividir todos los puntos por esa escala
4. Si `handedness == "Left"`, multiplicar la coordenada X por −1
5. Aplanar como x0,y0,z0,...,x20,y20,z20

Si esto difiere aunque sea en el orden, **el modelo devuelve basura sin lanzar ningún error**. Ya nos pasó una vez. No la reescribas "mejorándola".

### Umbrales

Los umbrales de confianza y el parámetro `k` viven en `config_rechazo.json`, **no** dentro del modelo. Se pueden ajustar sin reentrenar nada. No los hardcodees.

## Reglas de calidad

- **Sin lógica de negocio en Composables ni en ViewModels.** Va en casos de uso, en `domain`.
- Un único `UiState` por pantalla, expuesto como StateFlow.
- Estados de carga y error explícitos, nunca implícitos.
- Pruebas unitarias en `domain`; objetivo 40 % de cobertura en esa capa (RNF-19).
- Nada de `LiveData`, `AsyncTask` ni callbacks manuales.
- Nada de acceso a Room desde la UI.

## Accesibilidad (RNF-09 a RNF-11)

No es opcional ni "para el final". Parte de los usuarios son personas sordas:

- Toda retroalimentación sonora debe tener equivalente **visual o háptico**
- Contraste mínimo WCAG AA (4.5:1)
- Etiquetas de contenido en todos los controles interactivos, navegable con TalkBack
- Área táctil mínima 48×48dp
- El layout debe soportar el escalado de fuente del sistema

## Privacidad (RNF-06 a RNF-08)

- Todo el procesamiento ocurre en el dispositivo
- **La app no debe declarar el permiso de INTERNET en el manifiesto**
- No se almacena video; solo se procesan landmarks frame a frame
- El progreso vive solo en almacenamiento local

Si algo requiere red, está mal planteado.

## Limitaciones conocidas

- **H y R** tienen mayor tasa de falso rechazo. Causa identificada: articulación fina de dedos que no se resuelve a 120×120 en el dataset de entrenamiento. No es un bug de la app.
- **Ñ (`NN`)** quedó fuera del vocabulario del MVP por variación regional documentada en el dataset LSC70.
- El modelo se validó sobre recortes de dataset; la medición con cámara real está pendiente.

## Alcance: qué NO construir

- Traducción de conversaciones o comunicación bidireccional
- Cuentas de usuario, login, sincronización en la nube
- Funciones sociales (rankings, comparar racha con amigos)
- Avatar 3D que genere señas
- Señas dinámicas o con movimiento complejo (el modelo es de poses estáticas)
- Publicación en Play Store (se distribuye como APK académico)

## Prioridades

**Núcleo (Must):** lecciones por categoría, práctica con retroalimentación, progreso local, quiz simple, operación offline, accesibilidad mínima.

**Extensiones (Should/Could), en este orden:** racha de días → niveles e insignias → ampliar vocabulario → formatos alternativos de quiz → estadísticas detalladas → repaso espaciado → tema claro/oscuro.

No empieces extensiones antes de que el núcleo esté funcional y probado.

## Cómo responder

- Español.
- Si algo contradice el ERS, dilo en vez de implementarlo.
- Antes de reescribir código que ya funciona, pregunta.
- Si una decisión tiene alternativas razonables, propón y espera, no elijas por tu cuenta.
- Prefiere código simple sobre código clever. Es un proyecto académico que cuatro estudiantes tienen que poder explicar en una sustentación.

El encargo de interfaz está en docs/diseno/brief-ui.md y hay que leerlo antes de tocar la UI.
