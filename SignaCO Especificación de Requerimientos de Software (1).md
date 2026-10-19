# **SignaCO — Especificación de Requerimientos de Software**

**Versión:** 2.1 — ajuste del RF02 y registro de desviaciones (sección 8). La 2.0 se reestructuró tras el pivote de producto (de "asistente de comunicación" a "app de aprendizaje de LSC") **Referencia normativa:** estructura orientada por ISO/IEC/IEEE 29148 **Método de priorización:** MoSCoW (Must / Should / Could / Won't) **Métodos de verificación:** Demostración (D), Prueba (P), Inspección (I), Análisis (A) **Documento de alcance relacionado:** `SignaCO_anteproyecto_v2_app_aprendizaje.md`

## **0\. Qué cambió respecto a la versión 1.0**

La versión anterior de este documento especificaba una app de **comunicación en tiempo real** entre una persona sorda y una oyente (traducción a voz, respuesta del interlocutor por teclado/voz, historial de conversaciones). El proyecto pivotó a una **app de aprendizaje** de LSC de un solo usuario. Como consecuencia:

* Desaparecen por completo los módulos de "traducción de salida" (texto a voz) y "respuesta del interlocutor" (teclado/dictado) — ya no hay una segunda persona interactuando en tiempo real con la app.  
* Esto elimina el conflicto técnico que existía en la v1.0 entre el reconocimiento de voz offline (STT) y la restricción de no usar permiso de internet: **ya no hay STT en el sistema**.  
* El módulo de "modo práctica" deja de ser una función secundaria y pasa a ser el núcleo de todo el sistema.  
* El módulo de "historial de conversaciones" se reemplaza por un módulo de **progreso de aprendizaje**.  
* El vocabulario base baja de 30-40 a **18-20 señas** para el MVP (ver anteproyecto v2, sección 6), con 30-40 como extensión.

## **1\. Propósito y alcance**

Este documento define los requerimientos funcionales (RF), no funcionales (RNF) y las restricciones de diseño (RD) del MVP de SignaCO, conforme al alcance establecido en el anteproyecto v2: un vocabulario base de 18 a 20 señas LSC organizadas en lecciones por categoría, práctica con retroalimentación por visión artificial ejecutada en el dispositivo, quizzes, progreso local y operación offline. Toda afirmación de este documento debe ser verificable de forma objetiva.

## **2\. Actores del sistema**

| Actor | Descripción |
| ----- | ----- |
| Usuario aprendiz | Persona sorda u oyente que usa la app para aprender o reforzar LSC mediante lecciones, práctica y quizzes |
| Equipo de datos | Integrantes y voluntarios que graban el conjunto de datos (rol interno, no usuario de la app) |

## **3\. Requerimientos funcionales**

### **3.1 Módulo de contenido y lecciones**

| ID | Requerimiento | Prioridad | Verificación |
| ----- | ----- | ----- | ----- |
| RF01 | El sistema deberá organizar el vocabulario base en lecciones agrupadas por categoría (saludos, cortesía, necesidades elementales, ubicación, emergencia), cada una con 3 a 5 señas | Must | D |
| RF02 | El sistema deberá mostrar, para cada seña de una lección, una imagen o clip de referencia antes de solicitar la práctica *(modificado en v2.1: se retira la descripción textual; ver sección 8)* | Must | D |
| RF03 | El sistema deberá permitir navegar el catálogo completo de señas fuera del flujo de lecciones, a modo de diccionario de consulta | Should | D |

### **3.2 Módulo de captura y reconocimiento**

| ID | Requerimiento | Prioridad | Verificación |
| ----- | ----- | ----- | ----- |
| RF04 | El sistema deberá capturar video desde la cámara frontal o trasera, previa autorización explícita del permiso de cámara, con opción de cambiar entre ambas | Must | P |
| RF05 | El sistema deberá extraer en tiempo real los landmarks de manos del usuario mediante MediaPipe, procesando cada frame sin almacenar video | Must | P, A |
| RF06 | El sistema deberá clasificar la seña ejecutada dentro del vocabulario base de 18 a 20 señas LSC, usando un modelo TensorFlow Lite empaquetado localmente | Must | P |
| RF07 | El sistema deberá confirmar una seña únicamente cuando las predicciones se mantengan estables durante una ventana temporal configurable (suavizado temporal) | Must | P, A |
| RF08 | El sistema deberá descartar predicciones con confianza inferior a un umbral configurable (valor por defecto: 0.75) y presentar el estado "seña no reconocida" | Must | P |
| RF09 | El sistema deberá mostrar en todo momento el estado del reconocimiento mediante indicadores visuales: "buscando manos", "reconociendo" y "sin detección" | Must | D |
| RF10 | El usuario deberá poder iniciar y detener la captura de un intento mediante un control único, visible y accesible | Must | D, P |
| RF11 | El sistema deberá emitir una señal háptica (vibración breve) y visual al confirmar una seña correcta | Must | D |

### **3.3 Módulo de práctica y retroalimentación**

| ID | Requerimiento | Prioridad | Verificación |
| ----- | ----- | ----- | ----- |
| RF12 | Al ejecutar una seña dentro de una lección, el sistema deberá comparar la seña reconocida contra la seña objetivo y mostrar el resultado (acertada / no acertada) de forma inmediata | Must | P |
| RF13 | El sistema deberá permitir reintentar una seña no acertada sin salir de la lección | Must | D |
| RF14 | El sistema deberá marcar una seña como "dominada" tras un número configurable de aciertos consecutivos | Should | P |

### **3.4 Módulo de quiz**

| ID | Requerimiento | Prioridad | Verificación |
| ----- | ----- | ----- | ----- |
| RF15 | Al finalizar una lección, el sistema deberá presentar un quiz que solicite ejecutar frente a la cámara la seña correspondiente a una palabra mostrada en pantalla, reutilizando el motor de reconocimiento | Must | P |
| RF16 | El sistema deberá mostrar el resultado del quiz (aciertos / total) al finalizar | Must | D |
| RF17 | El sistema podrá ofrecer formatos adicionales de quiz (emparejar imagen-palabra, completar secuencia, modo contrarreloj) | Could | D |

### **3.5 Módulo de progreso**

| ID | Requerimiento | Prioridad | Verificación |
| ----- | ----- | ----- | ----- |
| RF18 | El sistema deberá registrar localmente, por seña, el número de intentos y aciertos | Must | P |
| RF19 | El sistema deberá registrar localmente qué lecciones y categorías han sido completadas | Must | P |
| RF20 | El sistema deberá mostrar al usuario un resumen de su progreso (señas dominadas, lecciones completadas) | Must | D |
| RF21 | El sistema deberá registrar y mostrar una racha de días consecutivos de práctica | Should | P |
| RF22 | El sistema podrá asignar niveles o insignias por categoría completada | Could | D |
| RF23 | El usuario deberá poder eliminar su progreso guardado, con confirmación previa | Should | P |
| RF24 | El sistema podrá mostrar estadísticas detalladas de progreso (tiempo practicado, precisión histórica por seña) | Could | D |
| RF25 | El sistema podrá implementar un modo de repaso espaciado que resalte señas no practicadas recientemente | Could | P |

### **3.6 Módulo de configuración**

| ID | Requerimiento | Prioridad | Verificación |
| ----- | ----- | ----- | ----- |
| RF26 | El usuario deberá poder ajustar el umbral de confianza y el tamaño de fuente | Should | P |
| RF27 | La aplicación podrá soportar tema claro y oscuro respetando la configuración del sistema | Could | D, I |

## **4\. Requerimientos no funcionales**

### **4.1 Rendimiento**

| ID | Requerimiento | Verificación |
| ----- | ----- | ----- |
| RNF01 | La latencia entre la ejecución estable de una seña y su retroalimentación en pantalla deberá ser inferior a 2 segundos en el dispositivo de referencia | P, A |
| RNF02 | El pipeline de cámara e inferencia deberá sostener al menos 15 frames por segundo en un dispositivo de gama media (mínimo 4 GB RAM, SoC de referencia a definir en el spike técnico) | P |
| RNF03 | El tamaño del modelo de clasificación empaquetado no deberá exceder 20 MB, y el APK final no deberá exceder 60 MB | I |

### **4.2 Calidad del modelo**

| ID | Requerimiento | Verificación |
| ----- | ----- | ----- |
| RNF04 | La precisión de clasificación deberá ser igual o superior al 85 % sobre un conjunto de prueba grabado por personas que no participaron en los datos de entrenamiento | P, A |
| RNF05 | La evaluación del modelo deberá realizarse separando los datos por persona (ninguna persona aparece simultáneamente en entrenamiento y prueba), documentando los resultados en el informe de pruebas | I, A |

> Nota de implementación: si el 85 % no se alcanza con el vocabulario de 18-20 señas, la palanca es reducir el vocabulario o revisar el umbral de confianza — no relajar la separación persona-independiente de RNF05, que es lo que garantiza que la métrica sea honesta.

### **4.3 Privacidad y seguridad**

| ID | Requerimiento | Verificación |
| ----- | ----- | ----- |
| RNF06 | Todo el procesamiento de video y landmarks deberá ejecutarse en el dispositivo; la aplicación no transmitirá video, imágenes ni landmarks a servidores externos | I, A |
| RNF07 | El núcleo funcional de la aplicación no deberá requerir el permiso de internet en el manifiesto | I |
| RNF08 | El progreso del usuario deberá almacenarse únicamente en almacenamiento local, y su borrado (RF23) deberá eliminar los datos de forma irreversible | P |

### **4.4 Usabilidad y accesibilidad**

| ID | Requerimiento | Verificación |
| ----- | ----- | ----- |
| RNF09 | Los flujos principales (lección, práctica, quiz, progreso) deberán ser navegables con TalkBack, con etiquetas de contenido en todos los controles interactivos | P |
| RNF10 | La interfaz deberá cumplir contraste mínimo WCAG AA (4.5:1) en textos y admitir escalado de fuente del sistema | I, P |
| RNF11 | Toda retroalimentación sonora relevante deberá tener equivalente visual o háptico, dado que parte de los usuarios puede no percibir audio | I, D |

### **4.5 Fiabilidad y disponibilidad**

| ID | Requerimiento | Verificación |
| ----- | ----- | ----- |
| RNF12 | Las funciones de lecciones, práctica, quiz y progreso deberán operar sin conexión a internet | P |
| RNF13 | Ante fallos de cámara, denegación de permisos o error de carga del modelo, la aplicación deberá mostrar un mensaje accionable y permitir reintento sin cierre inesperado | P |
| RNF14 | La aplicación deberá manejar la rotación de pantalla y la interrupción por llamadas sin perder el progreso de la lección activa | P |

### **4.6 Compatibilidad**

| ID | Requerimiento | Verificación |
| ----- | ----- | ----- |
| RNF15 | La aplicación deberá ejecutarse en Android 8.0 (API 26\) o superior | P, A |
| RNF16 | La interfaz deberá estar optimizada para orientación vertical en teléfonos de 5" a 7" | D |

### **4.7 Mantenibilidad y calidad del código**

| ID | Requerimiento | Verificación |
| ----- | ----- | ----- |
| RNF17 | El código deberá organizarse en arquitectura limpia con capas presentation / domain / data y dependencias unidireccionales hacia el dominio | I |
| RNF18 | La capa de presentación deberá seguir el patrón MVVM con estado de UI expuesto mediante StateFlow | I |
| RNF19 | La lógica de negocio de la capa domain (suavizado temporal, umbrales, cálculo de progreso) deberá tener cobertura de pruebas unitarias igual o superior al 40 % | P, A |
| RNF20 | La inyección de dependencias deberá realizarse con Hilt y la asincronía con Coroutines/Flow, sin callbacks manuales | I |
| RNF21 | El modelo de IA deberá ser reemplazable sin modificar las capas presentation y domain (interfaz SignClassifier con implementación concreta en data) | I, D |

## **5\. Restricciones de diseño**

| ID | Restricción |
| ----- | ----- |
| RD01 | Lenguaje Kotlin y UI declarativa con Jetpack Compose y Material 3 |
| RD02 | Cámara con CameraX; extracción de landmarks con MediaPipe Tasks; inferencia con TensorFlow Lite |
| RD03 | Persistencia local con Room; preferencias con DataStore |
| RD04 | Control de versiones en GitHub con revisión por pull request; CI en GitHub Actions ejecutando lint, pruebas unitarias y ensamblado del APK en cada push |
| RD05 | Todo participante en la grabación del dataset deberá firmar consentimiento informado; se almacenarán preferiblemente landmarks y no video crudo |
| RD06 | El vocabulario de señas deberá construirse a partir de fuentes públicas de LSC verificables, documentando la fuente de cada seña |

## **6\. Trazabilidad objetivos → requerimientos**

| Objetivo específico | Requerimientos que lo implementan |
| ----- | ----- |
| OE-1 Construir el dataset y contenido de lecciones | RF01-03 (insumo), RNF04, RNF05, RD05, RD06 |
| OE-2 Desarrollar la aplicación Android | RF04 a RF27, RNF01-03, RNF06-16, RD01-04 |
| OE-3 Evaluar el sistema | RNF04, RNF05, RNF19, más el plan de pruebas funcionales de RF04 a RF27 |

El estado de cada RF frente al código (dónde está implementado, qué prueba lo cubre y qué falta) se lleva en `docs/trazabilidad_rf.md`.

## **7\. Criterios de aceptación globales del MVP**

* Un usuario abre una lección, ejecuta las señas solicitadas frente a la cámara y recibe retroalimentación (acertada / no acertada) en menos de 2 segundos, sin conexión a internet.  
* Al finalizar una lección, el quiz asociado funciona de principio a fin y muestra un resultado (aciertos / total).  
* El progreso (señas dominadas, lecciones completadas) se guarda localmente y persiste al cerrar y reabrir la aplicación.  
* La aplicación completa el flujo lección → práctica → quiz sin cierres inesperados durante una sesión continua de 15 minutos.  
* La revisión del manifiesto y del código confirma la ausencia de transmisión de datos a servidores externos.

## **8\. Registro de cambios y desviaciones**

### **8.1 Cambios de requerimientos**

| Versión | RF | Cambio | Motivo | Aprobado por |
| ----- | ----- | ----- | ----- | ----- |
| 2.1 | RF02 | Se retira «y una descripción textual». La seña se enseña con su imagen de referencia. | Las descripciones nunca se validaron con una fuente de LSC y podían enseñar mal la seña. | Kevin y Anyela |

### **8.2 Desviaciones conocidas de la implementación**

Estas diferencias entre el texto de un RF y la implementación están documentadas en `docs/trazabilidad_rf.md` y pendientes de decisión del equipo:

| RF | Texto del ERS | Implementación |
| ----- | ----- | ----- |
| RF01 | Categorías de palabras (saludos, cortesía, necesidades, ubicación, emergencia) | El MVP enseña el abecedario: 5 lecciones de 3 a 4 letras en las categorías Vocales y Consonantes |
| RF08 | Umbral configurable, 0.75 por defecto | Umbral por letra en `config_rechazo.json`, ajustable sin recompilar |
| RF14 | Dominada tras N aciertos consecutivos, configurable | Dominada con 5 aciertos y una tasa de acierto de al menos 60 %; las constantes están en el código |
| RF26 | Ajustar umbral y tamaño de fuente | Solo el tamaño de texto y el contraste; los umbrales viven en `config_rechazo.json` |

### **8.3 Decisiones de diseño del quiz (RF15, RF16, RF17)**

El quiz de cada lección tiene dos partes: reconocimiento con imagen (cubre en parte el RF17, «emparejar imagen-palabra») y ejecución con cámara (RF15). La lección se aprueba con el 70 % de la parte con cámara, cuyos intentos se registran en el progreso (RF18). El detalle está en `docs/diseno/quiz-de-la-leccion.md`.
