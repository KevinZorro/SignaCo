Clean Architecture y MVVM en Android (Kotlin \+ Jetpack Compose), optimizado para procesamiento local en tiempo real de visión artificial (CameraX, MediaPipe y TensorFlow Lite) y persistencia offline. 

\[ Presentation Layer \] (Compose UI \+ ViewModels \+ StateFlow)  
           │  
           ▼  
     \[ Domain Layer \]    (Use Cases \+ Pure Domain Models \+ Classifier Interfaces)  
           ▲  
           │  
     \[ Data Layer \]      (TFLite \+ MediaPipe \+ Room DB \+ DataStore Repositories)

### Pipeline de Visión Artificial en Tiempo Real

1. CameraX (Data/Presentation) captura los frames sin persistir video.  
2. MediaPipe Tasks (Data) extrae las coordenadas de landmarks de la mano por frame.  
3. TFLiteSignClassifier (Data) consume los landmarks e infiere la seña.  
4. SmoothTemporalPredictionsUseCase (Domain) aplica suavizado temporal de predicciones y filtra por umbral de confianza (\$\> 0.75\$).  
5. LivePracticeViewModel (Presentation) expone el estado (PracticeUiState) hacia Compose via StateFlow y dispara la retroalimentación háptica/visual.

**2\. Estructura de Carpetas y Archivos** 

Ruta base del proyecto: app/src/main/java/com/signaco/app/

### **1\. Capa de Dominio (domain/) — El "Cerebro" de la App**

Contiene la lógica de negocio pura en Kotlin. No depende de la interfaz ni de librerías de Android.

| Carpeta | Archivo | Función / Responsabilidad |
| :---- | :---- | :---- |
| model/ (Modelos de datos) | Category.kt | Estructura de categorías (Saludos, Cortesía, etc.). |
|  | Lesson.kt | Estructura de cada lección. |
|  | SignItem.kt | Información de una seña específica (nombre, imágenes, landmarks esperados). |
|  | RecognitionState.kt | Estados del reconocedor (BUSCANDO\_MANOS, RECONOCIENDO, ACIERTO, NO\_RECONOCIDA). |
|  | Landmark.kt | Coordenadas \$(x, y, z)\$ de los puntos clave de la mano. |
|  | UserProgress.kt | Métricas de avance, racha de días e historial. |
| classifier/ (Contratos de IA) | SignClassifier.kt | Interfaz neutra del reconocedor de IA. Permite cambiar el modelo en el futuro sin romper la app (RNF21). |
| repository/ (Contratos de datos) | CategoryRepository.kt LessonRepository.kt ProgressRepository.kt | Interfaces que definen qué operaciones de datos se pueden hacer (guardar progreso, consultar lecciones, etc.). |
| usecase/ (Casos de uso) | ExtractLandmarksUseCase.kt | Solicita extraer las coordenadas de los puntos de la mano. |
|  | ClassifySignUseCase.kt | Envía los landmarks al clasificador para inferir la seña. |
|  | SmoothTemporalPredictionsUseCase.kt | Aplica la ventana de suavizado temporal (RF07) y valida la confianza \$\\ge 0.75\$ (RF08). |
|  | GetCategoryCatalogUseCase.kt | Obtiene el catálogo de lecciones organizadas. |
|  | UpdateProgressUseCase.kt | Registra si una seña fue dominada o si se completó una lección. |
|  | CalculateStreakUseCase.kt | Calcula la racha de días consecutivos de práctica (RF21). |

### 

### 

### 2\. Capa de Datos e IA (data/) — Integraciones y Persistencia

Implementa la captura de cámara, la visión artificial y la base de datos local.

| Subsistema | Archivo / Carpeta | Función / Responsabilidad |
| :---- | :---- | :---- |
| IA & Visión (data/ml/) | mediapipe/HandLandmarkExtractor.kt | Extrae en tiempo real las 21 coordenadas de la mano usando MediaPipe Tasks (RF05). |
|  | tflite/TFLiteSignClassifier.kt | Corre el modelo .tflite local para identificar la seña LSC (RF06). |
|  | PipelineManager.kt | Orquesta el flujo: Frame de Cámara \$\\rightarrow\$ MediaPipe \$\\rightarrow\$ TFLite. |
| Base de Datos (data/local/db/) | SignaCODatabase.kt | Configuración de Room Database para funcionamiento offline. |
|  | dao/LessonDao.kt dao/ProgressDao.kt | Consultas SQL para guardar aciertos, reintentos y lecciones completadas. |
|  | entity/ProgressEntity.kt entity/LessonEntity.kt | Tablas de la base de datos. |
| Preferencias (data/local/datastore/) | UserPreferencesDataStore.kt | Guarda configuraciones locales como el umbral de confianza y tamaño de letra (RF26). |
| Repositorios (data/repository/) | CategoryRepositoryImpl.kt LessonRepositoryImpl.kt ProgressRepositoryImpl.kt | Implementaciones concretas que conectan las tablas de Room y DataStore con la capa de Dominio. |

### 

### 3\. Capa de Presentación (presentation/) — Pantallas e Interfaz UI

Construida completamente con Jetpack Compose y ViewModels bajo el patrón MVVM.

#### A. Componentes Reutilizables y Cámara (presentation/components/ & presentation/camera/)

* CameraPreview.kt: Vista que renderiza la cámara frontal/trasera usando CameraX.  
* LandmarkOverlayCanvas.kt: Dibujo animado en pantalla con las líneas del esqueleto de la mano sobre el video.  
* RecognitionBadge.kt: Etiqueta visual que muestra el estado actual (Reconociendo, ¡Acertaste\!, Seña no reconocida).

#### B. Flujo de Pantallas del Prototipo

| Pantalla | Subcarpeta | Archivos Clave | Qué hace en la UI |
| :---- | :---- | :---- | :---- |
| 1 | permission/ | CameraPermissionScreen.kt | Solicita el permiso explícito de cámara antes de iniciar. |
| 2 | catalog/ | CatalogScreen.kt CatalogViewModel.kt | Muestra el catálogo de categorías (Saludos, Cortesía, etc.) y su avance. |
| 3 | lesson/ | LessonDetailScreen.kt LessonViewModel.kt | Muestra el detalle de la lección con la seña de referencia (video/imagen) y botón Empezar práctica. |
| 4 | practice/ | LivePracticeScreen.kt LivePracticeViewModel.kt HapticFeedbackManager.kt | Pantalla principal de práctica en vivo: junta la cámara, la superposición de landmarks, la retroalimentación inmediata y la vibración al acertar (RF11). |
| 5 | quiz/ | QuizScreen.kt QuizResultScreen.kt | Cuestionario al final de la lección para evaluar aciertos y mostrar la puntuación obtenida (RF15, RF16). |
| 6 | progress/ | ProgressSummaryScreen.kt ProgressViewModel.kt | Resumen de progreso: racha de días, señas dominadas y lecciones completadas (RF20, RF21). |

#### 4\. Raíz e Inyección de Dependencias (di/ y Archivos Base)

* SignaCOApp.kt: Punto de entrada global de la app donde se inicia Hilt (Inyección de dependencias).  
* MainActivity.kt: Actividad única que sostiene la navegación entre pantallas.  
* presentation/navigation/:  
  * NavGraph.kt: Define las rutas para ir de una pantalla a otra (Permisos \$\\rightarrow\$ Catálogo \$\\rightarrow\$ Lección \$\\rightarrow\$ Práctica en Vivo \$\\rightarrow\$ Quiz \$\\rightarrow\$ Progreso).  
* di/ (Módulos de Hilt):  
  * CameraModule.kt: Provee la cámara de Android.  
  * ClassifierModule.kt: Conecta la interfaz SignClassifier con la implementación TFLiteSignClassifier.  
  * DatabaseModule.kt: Provee la base de datos Room.  
  * RepositoryModule.kt: Inyecta los repositorios en los casos de uso.

3\. Especificación de Componentes del Núcleo 

| Componente | Capa | Responsabilidad |
| :---- | :---- | :---- |
| SignClassifier | Domain | Interfaz pura para desacoplar el motor de IA de la interfaz y permitir reemplazar el modelo sin impactar el resto del sistema (RNF21). |
| TFLiteSignClassifier | Data | Carga el archivo .tflite (inferencia local \$\\le 20\\text{ MB}\$), procesa el vector de entrada de landmarks y retorna la seña detectada con su probabilidad (RF06, RNF03). |
| HandLandmarkExtractor | Data | Emplea la librería MediaPipe Tasks para procesar cada frame proveniente de CameraX y emitir una lista de coordenadas \$3D\$ (\$x, y, z\$). |
| SmoothTemporalPredictionsUseCase | Domain | Implementa una cola circular (buffer temporal) para garantizar que una seña solo se valide si la predicción es consistente durante N frames consecutivos con confianza mayor a 0.75 (RF07, RF08). |
| LandmarkOverlayCanvas | Presentation | Componente Compose que superpone las líneas del esqueleto de la mano sobre el feed de video de la cámara en tiempo real. |
| LivePracticeViewModel | Presentation | Maneja la suscripción de datos desde el pipeline de visión, actualiza los estados visuales (Buscando manos / Reconociendo / Acierto / No acertada) y notifica al HapticFeedbackManager cuando se confirma un acierto (RF09, RF11, RF12). |

