# **SignaCO: Aplicación móvil de aprendizaje de la Lengua de Señas Colombiana con retroalimentación por visión artificial**

**Universidad Francisco de Paula Santander** Ingeniería de Sistemas — Aplicaciones Móviles \[Asignatura / docente / periodo — completar\]

**Integrantes** (equipo de 4 personas)

* Líder: Kevin David Zorro Hernández — 1152312  
* Anyela Jhohana Herrera Lobo — 1152256  
* Nefer Sneider Rojas Porras — \[código completo\]  
* Ángel \[apellido\] — \[código completo\]

> *Nota: completar apellidos y códigos faltantes antes de entregar — quedaron incompletos en el borrador original.*

## **1\. Introducción**

La comunicación es la condición que hace posible casi todas las demás formas de participación social: estudiar, trabajar, recibir atención médica o simplemente pedir indicaciones dependen de que dos personas compartan un canal común. Para la comunidad sorda de Colombia, ese canal es la Lengua de Señas Colombiana (LSC), reconocida jurídicamente como su lengua propia, pero desconocida por la inmensa mayoría de la población oyente. Esa asimetría no se resuelve únicamente con herramientas de traducción en el momento de la interacción: se resuelve, en buena parte, cerrando la brecha de conocimiento de fondo — que la población oyente simplemente no sabe LSC, y que muchas personas sordas, especialmente niños en zonas rurales, no cuentan con recursos estructurados y accesibles para aprender o reforzar su propia lengua.

Según cifras del Instituto Nacional para Sordos (INSOR), con base en la Encuesta de Calidad de Vida 2021, en Colombia habitan 459.784 personas sordas, y estimaciones de MinTIC citan cerca de 555.000 personas que emplean la lengua de señas como medio principal de comunicación. Frente a esa población, el país dispone de apenas unos 660 intérpretes certificados, concentrados en las grandes capitales. La mediación humana, aun siendo la solución ideal en contextos formales, no puede escalar a la velocidad que requeriría enseñar LSC a una fracción significativa de la población oyente ni reforzar el aprendizaje de miles de niños sordos.

En este contexto, el presente proyecto propone el diseño y desarrollo de **SignaCO**, una aplicación móvil Android para el **aprendizaje** de la Lengua de Señas Colombiana, dirigida por igual a personas oyentes que quieren aprenderla y a personas sordas que quieren reforzarla. La app organiza un vocabulario inicial de LSC en lecciones progresivas, con guía visual de cada seña, práctica frente a la cámara con retroalimentación automática (correcto / incorrecto) mediante visión artificial ejecutada íntegramente en el dispositivo, y elementos de gamificación (racha, niveles, quizzes) que sostienen la constancia del aprendizaje. Al ejecutarse localmente y sin depender de conexión a internet, la aplicación es viable para una población mayoritariamente ubicada en estratos socioeconómicos 1 y 2\.

## **2\. Antecedentes**

### **2.1 Antecedentes contextuales**

La comunidad sorda colombiana constituye una población significativa e históricamente desatendida. La Encuesta de Calidad de Vida 2021, reportada por el INSOR, contabiliza 459.784 personas sordas en el país, y el análisis institucional sobre el Censo DANE evidencia que más del 80 % de esta población habita en estratos 1 y 2\. Los intérpretes certificados se concentran en Bogotá y pocas ciudades principales, mientras municipios intermedios y zonas rurales carecen casi por completo de cobertura. Cúcuta figura entre las diez ciudades con mayor concentración de población sorda del país, lo que le da al proyecto usuarios reales disponibles para validación.

En el plano educativo, los lineamientos del INSOR para la educación rural de personas sordas señalan que el 72 % de los niños y jóvenes sordos entre 5 y 16 años de las zonas rurales está por fuera del sistema educativo — una cifra que muestra que la barrera comunicativa también golpea el acceso a la educación, incluida la del propio idioma de señas. Del otro lado, la población oyente rara vez tiene una vía accesible y estructurada para aprender LSC, lo que perpetúa la asimetría descrita en la introducción.

### **2.2 Antecedentes normativos**

La Ley 324 de 1996 reconoció la LSC como lengua oficial de la comunidad sorda y estableció la obligación estatal de garantizar medios de comunicación accesibles. La Ley 1618 de 2013 fijó disposiciones para el pleno ejercicio de los derechos de las personas con discapacidad, incluida la accesibilidad a la información. El Decreto 1421 de 2017 reglamentó la atención educativa inclusiva, exigiendo ajustes razonables en los entornos de formación — este último cobra especial relevancia para SignaCO, ya que el proyecto es, en esencia, una herramienta educativa.

### **2.3 Antecedentes tecnológicos e investigativos**

El reconocimiento automático de lengua de señas mediante visión por computadora lleva más de una década de desarrollo internacional, sobre todo en lengua de señas estadounidense (ASL). La disponibilidad de MediaPipe para extraer landmarks de manos en tiempo real directamente en el dispositivo ha reducido considerablemente el costo técnico de estos sistemas.

En Colombia, el antecedente más cercano es **Hablemos**, un desarrollo de estudiantes de la Universidad Nacional de Colombia (sede Medellín) que reconoce LSC mediante la cámara del dispositivo y la traduce a texto, audio y avatar de forma bidireccional; incluye además un diccionario de palabras pensado para aprender gestos y frases. Es, sin embargo, una herramienta centrada en la comunicación en vivo, con el aprendizaje como función secundaria, no una app de lecciones progresivas.

A nivel internacional existen varias apps de aprendizaje de lengua de señas: SignSchool y The ASL App ofrecen video-lecciones y ejercicios interactivos organizados por nivel para ASL; Lingvano se apoya en cursos estructurados; SignForDeaf Practice ofrece cuestionarios y seguimiento de desempeño dirigidos explícitamente a familiares, amigos y educadores sin conocimiento previo de lengua de señas — el mismo perfil de usuario general que busca SignaCO. Ninguna de ellas, sin embargo, valida la ejecución del usuario con visión artificial: la práctica se limita a cuestionarios de opción múltiple o emparejamiento, no a que la cámara confirme si la persona ejecutó bien la seña.

El vacío identificado es, entonces, doble: (i) no existe una aplicación de aprendizaje de LSC específicamente colombiana con lecciones progresivas; y (ii) ninguna de las apps de aprendizaje revisadas — para LSC o para otras lenguas de señas — combina contenido estructurado por niveles con retroalimentación de la ejecución del usuario mediante reconocimiento por cámara. SignaCO se ubica exactamente en esa intersección.

## **3\. Planteamiento del problema**

### **3.1 Descripción de la situación problemática**

La población oyente colombiana rara vez aprende LSC porque no existe una vía accesible, estructurada y gamificada para hacerlo — a diferencia del aprendizaje de idiomas hablados, que hoy cuenta con apps masivas de práctica diaria. Del lado de la comunidad sorda, en particular los niños en zonas rurales, el acceso a materiales estructurados para aprender o reforzar su propia lengua también es limitado. La escasez de intérpretes certificados (cerca de 660 en todo el país) agrava el problema, pero no es el núcleo de este proyecto: aunque hubiera más intérpretes, la asimetría de fondo — que la mayoría de la población oyente no sabe LSC — seguiría intacta. Enseñar LSC a más personas es una vía complementaria y más escalable que depender exclusivamente de mediación humana o de traducción en tiempo real.

### **3.2 Brecha tecnológica**

Las apps de aprendizaje de lengua de señas existentes validan la práctica de forma pasiva (cuestionarios, emparejar palabra-imagen), sin verificar si el usuario realmente ejecutó la seña correctamente. Las apps que sí reconocen señas por cámara (traductores como Hand Talk o Sign Language AI) están orientadas a la comunicación en tiempo real, no a la enseñanza progresiva, y ninguna cubre LSC de forma nativa. Además, las soluciones que dependen de procesamiento en la nube son poco adecuadas para una población con conectividad limitada (más del 80 % en estratos 1 y 2\) y con expectativas legítimas de privacidad sobre el video de sus manos.

### **3.3 Formulación del problema**

¿Cómo contribuir, mediante una aplicación móvil de aprendizaje con retroalimentación de señas por visión artificial ejecutada localmente en el dispositivo, a que personas oyentes y sordas de la ciudad de Cúcuta aprendan y practiquen la Lengua de Señas Colombiana de forma autónoma, estructurada y sostenida en el tiempo?

### **3.4 Delimitación**

* **Espacial:** ciudad de Cúcuta, Norte de Santander, con validación inicial en el entorno universitario de los autores.  
* **Temporal:** catorce (14) semanas del semestre académico, comprendiendo levantamiento de información, construcción del conjunto de datos y contenido de lecciones, desarrollo, pruebas y documentación.  
* **Temática:** un vocabulario base de LSC organizado en lecciones por categoría (saludos, cortesía, necesidades elementales, ubicación, una señal de emergencia), con práctica validada por cámara y elementos de gamificación. El sistema no pretende certificar competencia en LSC, cubrir su gramática completa, ni sustituir la enseñanza de un instructor sordo certificado.

## **4\. Justificación**

### **4.1 Justificación social**

Una app de aprendizaje ataca la causa de fondo — que la mayoría de la población oyente no sabe LSC — en lugar de solo mitigar sus síntomas en el momento de una conversación puntual. A diferencia de una herramienta de traducción en vivo, que solo genera valor si ambas personas tienen el teléfono a mano exactamente cuando lo necesitan, una app de aprendizaje construye una capacidad que la persona conserva después de cerrar la aplicación, se puede usar en solitario y de forma asíncrona, y un error del reconocimiento durante la práctica ("intenta de nuevo") tiene consecuencias mucho más bajas que un error durante una conversación real. Que Cúcuta esté entre las diez ciudades con mayor concentración de población sorda del país le da al proyecto pertinencia territorial inmediata, con población sorda real disponible para probar el contenido.

### **4.2 Justificación normativa e institucional**

El Decreto 1421 de 2017, que exige ajustes razonables en los entornos educativos, respalda directamente una herramienta cuyo propósito explícito es enseñar. La Ley 324 de 1996 y la Ley 1618 de 2013 refuerzan el mandato de accesibilidad comunicacional como derecho, no como concesión.

### **4.3 Justificación tecnológica y metodológica**

MediaPipe y TensorFlow Lite permiten construir el reconocimiento de práctica íntegramente en el dispositivo, sin enviar video a servidores externos — resolviendo a la vez la conectividad limitada de la población objetivo y la privacidad de procesar imágenes de sus manos. A diferencia de un sistema de comunicación en tiempo real, donde una clasificación errónea puede arruinar una conversación real, en un contexto de práctica el margen de error tolerable es mayor: una seña mal reconocida simplemente se reintenta, lo que hace el problema más abordable con un dataset construido por un equipo estudiantil en 14 semanas. El proyecto sigue siendo un ejercicio de ingeniería completo: construcción y curación de dataset propio, arquitectura por capas, procesamiento de cámara en tiempo real, persistencia local del progreso del usuario y lógica de gamificación.

### **4.4 Justificación académica e investigativa**

El vacío identificado en los antecedentes — ninguna app de aprendizaje de lengua de señas, ni siquiera para ASL, valida la práctica con visión artificial en lugar de cuestionarios pasivos — le da al proyecto un componente de aporte original, no solo de réplica de un patrón ya resuelto. El dataset de señas LSC, el contenido de lecciones y las métricas de precisión del reconocimiento de práctica serán resultados documentables y transferibles a trabajos futuros.

## **5\. Objetivos**

### **5.1 Objetivo general**

Diseñar e implementar una aplicación móvil Android para el aprendizaje de la Lengua de Señas Colombiana, dirigida a personas sordas y oyentes por igual, mediante lecciones progresivas, elementos de gamificación y retroalimentación de la ejecución de señas por visión artificial procesada localmente.

### **5.2 Objetivos específicos**

1. Construir un conjunto de datos y contenido de lecciones de LSC (vocabulario base organizado por categorías), grabado por el equipo y voluntarios del entorno académico.  
2. Desarrollar una aplicación Android con lecciones progresivas, práctica con reconocimiento de gestos en tiempo real y procesamiento local, seguimiento de progreso y elementos de gamificación.  
3. Evaluar el sistema mediante pruebas funcionales, midiendo la precisión del reconocimiento durante la práctica y la usabilidad percibida por los usuarios de prueba.

## **6\. Alcance**

### **6.1 Núcleo obligatorio del MVP (Must — lo que sí o sí se entrega en 14 semanas, equipo de 4 personas)**

* **Reconocimiento de práctica**: clasificación en tiempo real de un vocabulario base de **18 a 20 señas LSC** (saludos, cortesía, necesidades elementales, ubicación, una señal de emergencia), ejecutado íntegramente en el dispositivo.  
* **Lecciones por categoría**: cada lección presenta 3-5 señas con imagen o clip de referencia y descripción textual, seguidas de práctica frente a la cámara con retroalimentación inmediata (acertada / no acertada).  
* **Progreso local**: registro de señas dominadas e intentos por lección, almacenado exclusivamente en el dispositivo.  
* **Quiz simple** al final de cada categoría, reutilizando el mismo motor de reconocimiento ("¿cuál es la seña correcta?").  
* **Operación offline** completa del núcleo (lecciones, práctica, quizzes, progreso).  
* **Accesibilidad mínima viable**: TalkBack y contraste WCAG AA en las pantallas de lección y práctica; retroalimentación háptica al confirmar una seña correcta.

### **6.2 Extensiones deseables (Should / Could — la versión "completa" con gamificación, se incorpora en este orden si el cronograma lo permite)**

1. Racha de días consecutivos de práctica.  
2. Niveles o insignias por categoría completada.  
3. Ampliar el vocabulario base de 18-20 a 30-40 señas.  
4. Variedad de formatos de quiz (emparejar, completar secuencia, contrarreloj).  
5. Estadísticas de progreso más detalladas (tiempo practicado, precisión histórica por seña).  
6. Modo de repaso espaciado de señas ya aprendidas.  
7. Tema claro/oscuro y ajuste de umbral de confianza.

*Nota: el equipo pidió una mecánica "completa" (lecciones \+ quizzes \+ racha \+ niveles). Esa visión queda documentada aquí como hoja de ruta, pero el núcleo de 6.1 es lo que garantiza una entrega funcional en 14 semanas; los ítems de 6.2 se construyen en orden solo después de que el núcleo esté probado.*

### **6.3 Fuera de alcance (Won't)**

* Traducción de conversación en tiempo real o cualquier función de comunicación bidireccional entre dos personas (ese era el enfoque de la versión anterior del proyecto; queda descartado explícitamente).  
* Interpretación de gramática compleja de la LSC, expresiones faciales gramaticalizadas o variantes regionales extensas.  
* Reconocimiento simultáneo de varias personas, o en condiciones de baja iluminación/oclusión severa.  
* Contenido curricular con certificación oficial o equivalencias académicas formales.  
* Validación con personas sordas certificadas o intérpretes profesionales como evaluadores formales del sistema; las pruebas se hacen con el equipo y voluntarios del entorno académico, y la validación con la comunidad queda como trabajo futuro.  
* Publicación en Google Play Store; distribución como APK con fines académicos.  
* Cuentas en la nube, multiusuario o funciones sociales (comparar racha con amigos, rankings).

---

## **Referencias preliminares**

Instituto Nacional para Sordos – INSOR. Preguntas frecuentes: cifras de población sorda en Colombia (Encuesta de Calidad de Vida 2021). https://www.insor.gov.co/home/preguntas-frecuentes/

INSOR. Realidades de la población sorda en 10 ciudades del país. https://www.insor.gov.co/home/realidades-de-la-poblacion-sorda-en-10-ciudades-del-pais/

INSOR. Lineamientos para la educación de personas sordas en la ruralidad. https://educativo.insor.gov.co/wp-content/uploads/2023/05/Lineamientos-ruralidad-educacion-para-sordos.pdf

MinTIC – TIC sin Barreras. Intérpretes de lengua de señas, conectando mundos. https://ticsinbarreras.mintic.gov.co/791/w3-article-239337.html

Congreso de Colombia. Ley 324 de 1996; Ley 1618 de 2013; Decreto 1421 de 2017\.

Universidad Nacional de Colombia, Sede Medellín. Hablemos, app que traduce de lengua de señas a texto y audio en tiempo real. https://medellin.unal.edu.co/noticias/5361-hablemos-lengua-senas.html

Preply. Las mejores aplicaciones de lengua de signos ASL para todos los niveles. https://preply.com/es/blog/app-lengua-de-signos/

SignForDeaf Practice — Apps en Google Play. https://play.google.com/store/apps/details?id=com.akilliceviri.signfordeafpractice

