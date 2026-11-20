# Trazabilidad de requerimientos funcionales

Estado de cada requerimiento funcional del ERS (v2.1) contra el código de la app. Sirve para la
sustentación y para saber qué falta. Se actualiza en el mismo pull request que cambia un RF.

Última revisión: rama `Anyela_Cambios`, octubre de 2026.

**Estados:** ✅ Implementado · 🟡 Parcial · ⚠️ Implementado con desviación del ERS (ver la columna
de notas y la sección 8 del ERS) · ⬜ Pendiente.

## Matriz RF → código → prueba

### Contenido y lecciones

| RF | Prioridad | Estado | Dónde está | Cómo se prueba | Notas |
|---|---|---|---|---|---|
| RF01 | Must | ⚠️ | `core/database/CatalogoInicial.kt`, `feature/lecciones/` | `CatalogoInicialTest`, `ObtenerCatalogoDeLeccionesUseCaseTest` | 5 lecciones de 3 a 4 letras, agrupadas en Vocales y Consonantes. El ERS nombra categorías de palabras (saludos, cortesía…), pero el MVP enseña el abecedario. |
| RF02 | Must | ✅ | `core/senas/domain/model/ReferenciaVisual.kt`, `core/senas/data/ReferenciasEnRecursos.kt`, `core/ui/componentes/ReferenciaDeSena.kt`, `res/drawable-nodpi/` | `ReferenciasVisualesTest`, `RepositoriosConReferenciasTest` | Imagen de referencia de las 19 letras activas en la lista de la lección, «Practica conmigo», el diccionario y el quiz. Sin descripción textual (RF02 modificado en ERS v2.1). Fuentes en `docs/fuentes_senas.md`. |
| RF03 | Should | ✅ | `feature/diccionario/` | `BuscarEnDiccionarioUseCaseTest`, `DiccionarioViewModelTest` | Las 27 letras; búsqueda por letra y nombre. |

### Captura y reconocimiento

| RF | Prioridad | Estado | Dónde está | Cómo se prueba | Notas |
|---|---|---|---|---|---|
| RF04 | Must | ✅ | `feature/practica/presentation/camara/` | Manual en dispositivo | Cámara frontal y trasera, permiso con explicación y reintento. |
| RF05 | Must | ✅ | `feature/practica/data/camara/AnalizadorDeManosMediaPipe.kt` | `NormalizadorDeLandmarksTest` | No se guarda video: 63 floats por frame. |
| RF06 | Must | ✅ | `core/reconocimiento/data/TFLiteSignClassifier.kt` | `VocabularioActivoTest`, `FakeSignClassifierTest` | 19 letras activas de 28 clases. |
| RF07 | Must | ✅ | `feature/practica/domain/EstabilizadorDePredicciones.kt`, `CrearEstabilizadorUseCase.kt` | `EstabilizadorDePrediccionesTest`, `ProcesarFrameUseCaseTest`, `CrearEstabilizadorUseCaseTest` | `k`, `ventanaMs` (1.000 ms) y `fraccionMinima` (0.8) en configuración; votación por mayoría y coincidencia promediada. La ventana también afecta al quiz con cámara. RNF01 (< 2 s) sigue por medir en el teléfono de referencia. |
| RF08 | Must | ⚠️ | `core/reconocimiento/`, `config_rechazo.json` | `ObtenerUmbralDeSenaUseCaseTest` | Umbral por letra en `config_rechazo.json`, no un 0.75 único. |
| RF09 | Must | ✅ | `practica/presentation/camara/VisorDeCamara.kt` (píldora) | `PracticaCuentaRegresivaTest` | Buscando manos / reconociendo / sin detección, con texto. |
| RF10 | Must | ✅ | `VisorDeCamara.kt` (`ControlDeCaptura`), `PracticaViewModel` | `PracticaCuentaRegresivaTest` | El intento empieza solo tras una cuenta de 3 s. El mismo control cancela la cuenta, y «Iniciar intento» / «Detener» quedan de respaldo. |
| RF11 | Must | ✅ | `PracticaScreen.kt`, `QuizCamaraScreen.kt` | Manual en dispositivo | Sello y vibración opcional al acertar, en la práctica y en el quiz. |

### Práctica y retroalimentación

| RF | Prioridad | Estado | Dónde está | Cómo se prueba | Notas |
|---|---|---|---|---|---|
| RF12 | Must | ✅ | `ProcesarFrameUseCase`, `EvaluarIntentoUseCase` | `ProcesarFrameUseCaseTest`, `EvaluarIntentoUseCaseTest` | Acertada / otra letra / no reconocida / sin mano. |
| RF13 | Must | ✅ | `PracticaViewModel.reintentarSena`, `DecidirPasoTrasElAciertoUseCase` | `DecidirPasoTrasElAciertoUseCaseTest`, `PracticaAciertoTest` | Reintento con cuenta regresiva. Dos caminos: lección completa (indicación → cámara por letra) y letra suelta (vuelve a la lista). |
| RF14 | Should | 🟡 | `feature/progreso/domain/model/ProgresoSena.kt` | `ProgresoSenaTest` | «Dominada» = 5 aciertos y 60 % de tasa, no N aciertos consecutivos. Las constantes están en código, no son configurables. |

### Quiz

| RF | Prioridad | Estado | Dónde está | Cómo se prueba | Notas |
|---|---|---|---|---|---|
| RF15 | Must | ✅ | `feature/quiz/domain/usecase/GenerarQuizDeCamaraUseCase.kt`, `EvaluarIntentoDePreguntaUseCase.kt`, `feature/quiz/presentation/QuizCamaraScreen.kt` | `GenerarQuizDeCamaraUseCaseTest`, `EvaluarIntentoDePreguntaUseCaseTest`, `CalificarQuizDeCamaraUseCaseTest`, `QuizCamaraPantallaTest` | 5 letras de la lección frente a la cámara, con el mismo reconocimiento y hasta 3 intentos. Los intentos se registran en el progreso. Aprueba la lección con 70 %. Diseño en `docs/diseno/quiz-de-la-leccion.md`. |
| RF16 | Must | ✅ | `feature/quiz/presentation/ResultadoQuizScreen.kt`, `ObtenerDesgloseDeQuizUseCase.kt` | `ObtenerDesgloseDeQuizUseCaseTest` | Aciertos / total con la cámara, parte con imagen, desglose por letra y «Repasar». |
| RF17 | Could | 🟡 | `feature/quiz/domain/usecase/GenerarQuizUseCase.kt`, `QuizScreen.kt` | `GenerarQuizUseCaseTest`, `QuizConImagenFlujoTest` | Hecho: «emparejar imagen-letra» (parte 1 del quiz). Faltan: completar secuencia y contrarreloj. |

### Progreso

| RF | Prioridad | Estado | Dónde está | Cómo se prueba | Notas |
|---|---|---|---|---|---|
| RF18 | Must | ✅ | `ProgresoDao.registrarIntento`, `RegistrarIntentoUseCase` | `RegistrarIntentoUseCaseTest`, `SignaCoDatabaseTest` | La práctica y el quiz con cámara registran intentos. |
| RF19 | Must | 🟡 | `lecciones_completadas`, `RegistrarResultadoDeQuizUseCase` | `RegistrarResultadoDeQuizUseCaseTest` | Lecciones completadas sí. «Categorías completadas» no se registra ni se muestra. |
| RF20 | Must | ✅ | `feature/progreso/` | `ObtenerResumenDeProgresoUseCaseTest`, `ProgresoViewModelTest` | Señas dominadas y lecciones completadas. |
| RF21 | Should | ✅ | `ActualizarRachaUseCase`, `actividad_diaria` | `ActualizarRachaUseCaseTest` | Racha y días activos de la semana. |
| RF22 | Could | ⬜ | — | — | Niveles o insignias por categoría. |
| RF23 | Should | ✅ | `BorrarProgresoUseCase`, `ProgresoDao.borrarTodo` | `BorrarProgresoUseCaseTest` | Con diálogo de confirmación. |
| RF24 | Could | ⬜ | — | — | Estadísticas detalladas. |
| RF25 | Could | ⬜ | — | — | Repaso espaciado. |

### Configuración

| RF | Prioridad | Estado | Dónde está | Cómo se prueba | Notas |
|---|---|---|---|---|---|
| RF26 | Should | 🟡 | `feature/ajustes/presentation/TamanoYContrasteScreen.kt` | Manual | Tamaño de texto y alto contraste, sí. Ajustar el umbral desde la app, no: los umbrales viven en `config_rechazo.json` por decisión del proyecto. |
| RF27 | Could | ⬜ | `core/ui/theme/Theme.kt` | — | Solo tema claro. |

## Requerimientos no funcionales tocados en la rama `Anyela_Cambios`

| RNF | Qué se hizo |
|---|---|
| RNF06-08 | Sin permiso de INTERNET. Las imágenes van empaquetadas en la app y no se guarda video. |
| RNF09 | TalkBack anuncia la cuenta regresiva y el inicio del intento. La imagen del quiz no nombra la letra. |
| RNF11 | El acierto y el fallo se comunican con texto, ícono, sello y vibración opcional. |
| RNF13 | Errores de cámara y de modelo con reintento en la práctica y en el quiz con cámara. |
| RNF19 | Cobertura de domain medida con `./gradlew coberturaDomain -Pcobertura`: 93,0 % de líneas al inicio y 93,8 % al final. |
| RD06 / RD05 | `docs/fuentes_senas.md`: autoría de las imágenes. Pendientes: la fuente de LSC de cada seña y el consentimiento. |

## Cambios de la rama `feat/recordatorios-y-promedio-senas`

| Cambio | RF / RNF | Componentes principales | Notas |
|---|---|---|---|
| Ventana de 1 segundo y votación por mayoría | RF07 | `EstabilizadorDePredicciones.kt`, `CrearEstabilizadorUseCase.kt`, `ProcesarFrameUseCase.kt` | Votación por mayoría (≥ 80 %) y coincidencia promediada en 1.000 ms. La ventana también afecta al quiz con cámara. RNF01 (< 2 s) sigue por medir en el teléfono de referencia. |
| Recordatorios diarios locales | Pedido del equipo; respeta RNF06–RNF08 | `core/notificaciones/`, `AjustesScreen.kt`, `AjustesViewModel.kt`, `AndroidManifest.xml` | Alarma inexacta diaria (~6 p.m.), sin red/INTERNET, rotación determinista de mensajes por día. Ningún RF de esta matriz cubre los recordatorios. |
| QA de recordatorios y estabilidad | RNF19 | `EstabilizadorDePrediccionesTest`, `ProcesarFrameUseCaseTest`, `CrearEstabilizadorUseCaseTest`, `EvaluarEnvioDeRecordatorioUseCaseTest`, `SincronizarRecordatoriosUseCaseTest`, `AjustesViewModelTest` | Cobertura de casos borde (ruido aislado, 25 %, 999 vs 1000 ms, pestillo, reinicio, rotación de mensajes, timestamps y sincronización). |

## Cambios de la rama `Anyela_Cambios`

| Cambio | RF | Commits principales |
|---|---|---|
| Medición de cobertura de domain | RNF19 | `chore: medir la cobertura de domain con JaCoCo` |
| Imágenes de referencia de las 19 letras | RF02, RD06 | `feat: asociar cada seña con su imagen de referencia por letra`, `feat: mostrar las imágenes de referencia…`, `feat: asociar las 19 letras activas…` |
| Práctica de letra suelta y de lección completa | RF13 | `fix: la práctica de una letra suelta vuelve a la lista…`, `feat: mostrar la indicación de cada letra antes de la cámara…` |
| Quiz de reconocimiento con imagen | RF17 | `feat: quiz de reconocimiento con imagen y cuatro opciones` |
| Quitar descripciones y migración de Room 2 → 3 | RF02 | `refactor: quitar la descripción de texto…`, `feat: migración de Room 2 a 3…` |
| Cuenta regresiva antes de cada intento | RF10 | `feat: iniciar automáticamente el intento tras una cuenta regresiva de 3 segundos` |
| Quiz con cámara y resultado de dos partes | RF15, RF16, RF18, RF19 | `feat: quiz con cámara al final de la lección (RF15)`, `feat: resultado del quiz con las partes de imagen y cámara…` |

## Pendiente en relación con los RF

**Must**

- **RF19, categorías completadas:** registrar y mostrar cuándo se completan todas las lecciones
  de una categoría (Vocales, Consonantes). Hoy solo se guardan lecciones.
- **RF01:** confirmar con el equipo que las categorías del abecedario (Vocales / Consonantes)
  reemplazan a las del ERS, y ajustar el texto del RF01.
- **RF08:** dejar registrado en el ERS que el umbral es por letra en `config_rechazo.json`.

**Should**

- **RF14:** decidir si «dominada» sigue siendo 5 aciertos con 60 % o pasa a N aciertos
  consecutivos, y si la regla se vuelve configurable.
- **RF26:** ajustar el umbral desde la app choca con la regla de no tocar `config_rechazo.json`.
  Hay que decidir si se descarta o si se hace un ajuste acotado (por ejemplo, un nivel
  «estricto / normal») sin cambiar el archivo.

**Could (extensiones, en el orden de prioridad del proyecto)**

- RF22 niveles e insignias → RF17 formatos restantes (secuencia, contrarreloj) → RF24
  estadísticas → RF25 repaso espaciado → RF27 tema oscuro.

**Verificación en dispositivo** (no son RF, pero el ERS los exige para el MVP)

- RNF01 latencia < 2 s y RNF02 ≥ 15 fps, medidos en el teléfono de referencia.
- RNF04 exactitud con cámara real (hoy está medida sobre recortes del dataset).
- RNF14 rotación y llamada entrante sin perder la lección activa.
- Correr las pruebas instrumentadas (migración, repositorios y Compose) en el emulador o en el
  CI de `master`.
