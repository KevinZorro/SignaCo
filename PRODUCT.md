# Product

<!-- impeccable:product-schema 1 -->

## Platform

android

## Users

Una sola persona por dispositivo que aprende el abecedario de la Lengua de Señas Colombiana (LSC). Por igual, personas oyentes que quieren aprender LSC y personas sordas que quieren reforzarla. La usan a ratos cortos, con el teléfono apoyado o en la mano, frente a la cámara frontal.

## Product Purpose

Enseñar el abecedario dactilológico de la LSC: abrir una lección, ver las señas de referencia, practicarlas frente a la cámara con retroalimentación inmediata (acertada / no acertada / mano no detectada), hacer un quiz al final y ver el progreso guardado en el dispositivo. El éxito es que la persona domine las letras del vocabulario activo.

## Positioning

El reconocimiento ocurre en el teléfono, sin red: la app solo lee 21 puntos de la mano cuadro a cuadro y nunca guarda ni envía video. No es una app de traducción ni de comunicación entre dos personas.

## Operating Context

- Proyecto académico (UFPS, Ingeniería de Sistemas), equipo de 4 personas, distribuido como APK académico, no en Play Store.
- Funciona sin conexión; la app no declara el permiso de INTERNET.
- Práctica con CameraX + MediaPipe HandLandmarker + modelo TFLite propio (28 clases), con umbrales y estabilidad `k` en `config_rechazo.json`.

## Capabilities and Constraints

- Lecciones por categoría (Vocales, Consonantes), práctica con cámara, quiz de opción múltiple, progreso y racha locales (Room + DataStore), diccionario de las 27 letras.
- Vocabulario activo: 19 letras; el resto (incluida la Ñ) se muestra en el diccionario pero no se practica.
- Solo poses estáticas. H y R tienen más falso rechazo; no es un bug de la app.
- Fuera de alcance: traducción, cuentas, login, nube, funciones sociales, avatar 3D, señas dinámicas.
- Stack fijo del ERS: Kotlin, Jetpack Compose + Material 3, Clean Architecture, MVVM con StateFlow, Hilt, Room, DataStore, Navigation Compose.

## Brand Commitments

Nombre SignaCO. Identidad visual «Cuaderno» definida en Figma (archivo SignaCO); ver DESIGN.md.

## Evidence on Hand

- Capturas de diseño en `docs/diseno/pantallas/` y medidas en `docs/diseno/signaco-figma-script.js`.
- No hay imágenes ni videos de demostración de las señas en la app: no inventarlos.

## Product Principles

- Una sola cosa por aprender en pantalla a la vez.
- Todo en el dispositivo: si algo requiere red, está mal planteado.
- Tres desenlaces distintos en la práctica (acierto, no reconocida, sin mano), cada uno dicho de forma distinta.
- Explicable por cuatro estudiantes en una sustentación: simple antes que ingenioso.

## Accessibility & Inclusion

- Toda señal sonora tiene equivalente visual o háptico (RNF-09); parte de las personas usuarias son sordas.
- Contraste mínimo WCAG AA 4.5:1 (RNF-10).
- Etiquetas para TalkBack en todos los controles, área táctil mínima 48×48 dp y soporte del escalado de fuente del sistema (RNF-11).
