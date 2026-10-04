---
name: SignaCO
description: Cuaderno para aprender el abecedario de la LSC con la cámara del teléfono.
colors:
  tinta: "#2438B8"
  tinta-oscura: "#1B2459"
  tinta-suave: "#56608A"
  cuadricula: "#D6DEEA"
  borde: "#BAC6DA"
  resaltador: "#FFE14A"
  correccion: "#CF2A3A"
  papel: "#FFFFFF"
  tinta-tonal: "#E8EBF8"
  superficie-tonal: "#F3F5F9"
  camara-alta: "#2E365F"
  camara-baja: "#1F2649"
  silueta: "#3B4371"
  panel-camara: "#141A3E"
typography:
  numero:
    fontFamily: "Atkinson Hyperlegible Next"
    fontSize: "120sp"
    fontWeight: 800
    lineHeight: 1.0
    letterSpacing: "-3sp"
  palabra:
    fontFamily: "Atkinson Hyperlegible Next"
    fontSize: "48sp"
    fontWeight: 800
    lineHeight: 1.05
    letterSpacing: "-1sp"
  titulo:
    fontFamily: "Atkinson Hyperlegible Next"
    fontSize: "36sp"
    fontWeight: 800
    lineHeight: 1.1
    letterSpacing: "-1sp"
  feedback:
    fontFamily: "Atkinson Hyperlegible Next"
    fontSize: "24sp"
    fontWeight: 800
    lineHeight: 1.2
  seccion:
    fontFamily: "Atkinson Hyperlegible Next"
    fontSize: "21sp"
    fontWeight: 700
    lineHeight: 1.25
  cuerpo:
    fontFamily: "Atkinson Hyperlegible Next"
    fontSize: "16sp"
    fontWeight: 400
    lineHeight: 1.45
  secundario:
    fontFamily: "Atkinson Hyperlegible Next"
    fontSize: "14sp"
    fontWeight: 400
    lineHeight: 1.4
  leyenda:
    fontFamily: "Atkinson Hyperlegible Next"
    fontSize: "12sp"
    fontWeight: 700
    lineHeight: 1.35
  boton:
    fontFamily: "Atkinson Hyperlegible Next"
    fontSize: "18sp"
    fontWeight: 700
    lineHeight: 1.2
rounded:
  pieza: "8dp"
  contenedor: "16dp"
  contenedor-grande: "24dp"
  completo: "999dp"
spacing:
  xxs: "4dp"
  xs: "8dp"
  sm: "12dp"
  md: "16dp"
  lg: "24dp"
  xl: "32dp"
  xxl: "48dp"
  margen: "20dp"
components:
  boton-principal:
    backgroundColor: "{colors.tinta}"
    textColor: "{colors.papel}"
    typography: "{typography.boton}"
    rounded: "{rounded.completo}"
    height: "56dp"
  boton-tonal:
    backgroundColor: "{colors.tinta-tonal}"
    textColor: "{colors.tinta}"
    typography: "{typography.boton}"
    rounded: "{rounded.completo}"
    height: "56dp"
  boton-texto:
    textColor: "{colors.tinta}"
    typography: "{typography.boton}"
    height: "48dp"
  tarjeta-de-palabra:
    backgroundColor: "{colors.papel}"
    textColor: "{colors.tinta}"
    typography: "{typography.palabra}"
    rounded: "{rounded.contenedor}"
  grupo-tonal:
    backgroundColor: "{colors.superficie-tonal}"
    textColor: "{colors.tinta-oscura}"
    rounded: "{rounded.contenedor}"
    padding: "16dp"
---

# Design System: SignaCO

## Overview

**Creative North Star: "El Cuaderno"**

SignaCO se ve como un cuaderno de clase bien llevado: papel blanco, tinta azul para lo que ya se sabe y un resaltador amarillo solo sobre lo que se está aprendiendo ahora. La cuadrícula del cuaderno aparece únicamente donde significa algo (el cuaderno de progreso y la tarjeta de la palabra); el resto de la app es papel liso con mucho aire.

Es una app de Material 3 en Android: navegación inferior, atrás del sistema, áreas táctiles de 48 dp. La identidad vive en la paleta, en la tipografía Atkinson Hyperlegible Next y en la precisión del espaciado, no en controles inventados. El nivel de acabado buscado es el de iOS o HyperOS: pocos elementos compitiendo, alineación exacta y movimiento corto.

**Key Characteristics:**
- Un solo foco por pantalla.
- Amarillo pequeño y escaso, nunca como bloque de fondo.
- Superficies tonales y separadores finos en lugar de cajas con borde.
- Máximo tres tamaños de texto por pantalla.

## Colors

Papel blanco y tinta azul, con un resaltador amarillo y un rojo de corrección usados con avaricia.

### Primary
- **Tinta** (#2438B8): acciones, botón principal, señas dominadas, indicador de navegación (en su versión tonal).

### Secondary
- **Resaltador** (#FFE14A): solo lo que se aprende ahora: segmento actual del progreso, cuadro de la seña en curso, puntos de la mano mientras se reconoce. Siempre acompañado de texto o ícono.

### Tertiary
- **Corrección** (#CF2A3A): solo errores. En la práctica sin coincidencia va en el ícono del título y en el tramo de la barra; en progreso, solo en el diálogo de borrado.

### Neutral
- **Tinta oscura** (#1B2459): texto principal.
- **Tinta suave** (#56608A): texto secundario y acciones de texto discretas.
- **Cuadrícula** (#D6DEEA): líneas del cuaderno, divisores, marco de la cámara.
- **Borde** (#BAC6DA): contorno de lo pendiente (cuadro «por aprender»).
- **Papel** (#FFFFFF): fondo.
- **Tinta tonal** (#E8EBF8) y **Superficie tonal** (#F3F5F9): agrupaciones, indicador de navegación, buscador, miniaturas.
- **Cámara** (#2E365F → #1F2649, silueta #3B4371, panel #141A3E): solo en el visor de la práctica.

### Named Rules
**The Highlighter Rule.** El amarillo marca únicamente lo que se aprende ahora y en áreas pequeñas. Si un bloque amarillo no se puede leer como «esto es lo que estás aprendiendo», sobra.

**The Notebook Rule.** La cuadrícula es para el cuaderno de progreso y la tarjeta de la palabra. Ninguna otra superficie la lleva.

**The Red Pen Rule.** El rojo es la corrección del profesor: solo en errores, nunca decorativo ni en acciones destructivas fuera del diálogo de confirmación.

## Typography

**Display Font:** Atkinson Hyperlegible Next (empaquetada en res/font)
**Body Font:** Atkinson Hyperlegible Next

**Character:** una sola familia hiperlegible en tres pesos (400, 700, 800). La jerarquía sale del tamaño y del peso.

### Hierarchy
- **Número** (800, 120sp, 1.0): el puntaje del quiz.
- **Palabra** (800, 48sp, 1.05): la seña que se practica.
- **Título** (800, 36sp, 1.1): título de pantalla.
- **Feedback** (800, 24sp, 1.2): «¡Acertaste!», «No reconocimos la seña».
- **Sección** (700, 21sp, 1.25): títulos de fila y de sección.
- **Cuerpo** (400, 16sp, 1.45): descripciones.
- **Secundario** (400, 14sp, 1.4): subtítulos y notas.
- **Leyenda** (700, 12sp, 1.35): leyendas y etiquetas cortas.
- **Botón** (700, 18sp, 1.2): texto de botones.

### Named Rules
**The Three Sizes Rule.** Ninguna pantalla usa más de tres tamaños de texto.

## Layout

Una columna con márgenes laterales de 20 dp. Espaciado en escala de 4 dp (4, 8, 12, 16, 24, 32, 48) y el mismo espacio entre elementos equivalentes en todas las pantallas. Edge-to-edge con WindowInsets; la barra de estado y la de gestos de Figma no se dibujan. Debe aguantar la fuente del sistema al 130 % y 200 %.

## Elevation & Depth

Plano. La profundidad se da con superficies tonales. La única sombra es la de la hoja inferior del resultado de la práctica.

## Shapes

Radios de 8 dp en piezas pequeñas (cuadros de progreso, miniaturas) y de 16 a 24 dp en contenedores. Botones de 56 dp de alto totalmente redondeados. Íconos de 24 dp con un mismo trazo (Material Symbols Outlined) como vector drawables.

## Components

### Buttons
- **Shape:** píldora completa, 56 dp de alto.
- **Primary:** fondo Tinta, texto Papel en estilo Botón. Una sola acción principal por pantalla.
- **Tonal:** fondo Tinta tonal, texto Tinta (p. ej. «Detener»).
- **Text:** sin fondo, texto Tinta o Tinta suave, área táctil de 48 dp.
- **Estados:** presionado (capa de estado de Material), deshabilitado, cargando.

### Chips
- **Style:** FilterChip de Material 3; solo el seleccionado lleva relleno tonal de Tinta; el resto neutro y sin amarillo. La fila se desplaza en horizontal.

### Cards / Containers
- **Corner Style:** 16 a 24 dp.
- **Background:** superficie tonal, sin borde exterior.
- **Shadow Strategy:** ninguna (ver Elevation).
- **Internal Padding:** 16 dp; separadores finos en Cuadrícula entre filas.

### Inputs / Fields
- **Style:** buscador sobre superficie tonal, píldora, sin contorno grueso.

### Navigation
- **Style:** NavigationBar de Material 3 con tres destinos (Aprender, Diccionario, Progreso). Indicador tonal de Tinta; ícono y etiqueta activos en Tinta.

### Tarjeta de la palabra
Cuadrícula sin borde exterior, la palabra en Palabra/800 en Tinta y la indicación en Secundario.

### Visor de la práctica
Vista previa real de CameraX con marco punteado en Cuadrícula, píldora de estado en Papel con texto Tinta oscura y un indicador pequeño, puntos de la mano dibujados con Canvas y un medidor de coincidencia con la marca del umbral.

## Do's and Don'ts

### Do:
- **Do** dejar el amarillo en el segmento actual, en el cuadro de la seña en curso y en los puntos de la mano mientras se reconoce.
- **Do** agrupar con superficies tonales y separadores finos.
- **Do** mover con duraciones de 150 a 250 ms y curvas de desaceleración, sin rebotes.

### Don't:
- **Don't** usar el amarillo como fondo de tarjeta ni como indicador de navegación.
- **Don't** poner cuadrícula fuera del cuaderno de progreso y la tarjeta de la palabra.
- **Don't** usar el rojo fuera de los errores y del diálogo de borrado.
- **Don't** dibujar contornos gruesos en botones secundarios ni en selectores.
