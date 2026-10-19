# Quiz de la lección: reconocimiento con imagen y ejecución con cámara (RF15)

Decisiones de diseño aprobadas por Anyela antes de implementar. Si algo de aquí cambia,
se actualiza este documento en el mismo pull request que cambia el código.

## Por qué cambia el quiz

- El quiz anterior era de opción múltiple sobre descripciones de texto. Las descripciones se
  quitaron porque nunca se validaron con material de LSC (ver «Desviación del ERS» abajo).
- Se podía aprobar una lección sin haber hecho ninguna seña frente a la cámara: el quiz no
  registraba intentos, así que el progreso (practicada / dominada) no cambiaba.

## Flujo

```
Lección ─ «Hacer el quiz» ─▶ Parte 1: imagen ─▶ Parte 2: cámara ─▶ Resultado
Práctica (última letra) ─ «Terminar y hacer el quiz» ─┘
```

1. **Parte 1, reconocimiento con imagen** (`QuizScreen`): se muestra la imagen de referencia de
   una seña y cuatro letras para elegir.
2. **Parte 2, ejecución con cámara** (`QuizCamaraScreen`, RF15): se muestra solo la letra y la
   persona la hace frente a la cámara.
3. **Resultado** (`ResultadoQuizScreen`): las dos partes, con el desglose por letra.

## Parte 1: reconocimiento con imagen

| Decisión | Valor |
|---|---|
| Preguntas | Una por letra de la lección que tenga imagen, máximo 4, sin repetir |
| Opciones | 4 letras; las incorrectas salen primero de la misma lección y se completan con el vocabulario activo |
| Letras sin imagen | No se preguntan. Si ninguna letra de la lección tiene imagen, la parte 1 se salta |
| Registra intentos de seña | No: reconocer una imagen no es ejecutar la seña |
| Cuenta para aprobar | No. Se muestra en el resultado |
| TalkBack | La imagen se anuncia como «Imagen de la seña que debes reconocer», sin nombrar la letra |

## Parte 2: ejecución con cámara (RF15)

| Decisión | Valor |
|---|---|
| Preguntas | 5 |
| Qué letras | Las de la lección, al azar. Cada letra sale al menos una vez; el resto se completa al azar |
| Repetición | Una letra puede repetirse, pero nunca en dos preguntas seguidas |
| Qué se muestra | Solo la letra («Letra B»). **No** la imagen de referencia: sería dar la respuesta |
| Antes de cada intento | La misma cuenta regresiva de 3 segundos de la práctica |
| Intentos por pregunta | Hasta 3 intentos confirmados |
| Acierto | La pregunta cuenta como acertada y se pasa a la siguiente |
| Fallo | Se muestra qué letra se vio y cuántos intentos quedan; la cuenta vuelve a empezar sola |
| Tercer fallo o «Saltar pregunta» | La pregunta cuenta como error y se pasa a la siguiente |
| «No reconocimos la seña» | No consume intento, igual que en la práctica |
| Registra intentos de seña | Sí, cada intento confirmado, acertado o no. Actualiza practicada y dominada |

El reconocimiento es el mismo de la práctica (`ProcesarFrameUseCase`, el estabilizador con la
`k` de `config_rechazo.json` y `EvaluarIntentoUseCase`). No se toca el modelo, el dataset ni la
configuración de rechazo.

## Aprobación de la lección

- La lección se aprueba si la parte de cámara llega al **70 %** (4 de 5). Con 3 preguntas
  harían falta las tres, porque 2 de 3 es 66 %.
- En `lecciones_completadas` se guarda el puntaje de la parte de cámara. No hay cambio de base
  de datos.
- La parte de imagen no aprueba ni reprueba la lección.
- Contestar el quiz cuenta como actividad del día para la racha, se apruebe o no.

## Resultado

- Número grande: aciertos de la parte de cámara («4 de 5») con el sello si se aprobó.
- Debajo, texto secundario: «Reconocimiento con imagen: 3 de 4» (si hubo parte 1).
- Desglose por letra de la parte de cámara, agrupando las repetidas («Letra B — 2 de 2»), con la
  insignia «Dominada» ya actualizada por los intentos del quiz.
- «Repasar»: las letras con algún error. Cada una abre su indicación como letra suelta
  (indicación → cámara → volver).
- Acciones: «Repetir el quiz» (empieza de nuevo por la parte 1) y «Ver mi progreso» como botón
  de texto.

## Accesibilidad

- La cuenta regresiva es una región viva: TalkBack anuncia cada número y el inicio del intento.
- El acierto y el fallo se dicen con texto e ícono, no solo con color; el acierto vibra si la
  persona lo tiene activado.
- Áreas táctiles de 48 dp y textos en `strings.xml`.

## Para revisar con Nefer

Estas pantallas no están en Figma y siguen el estilo de las existentes:

- `QuizCamaraScreen`: misma estructura que la práctica, sin imagen y sin botón de pista.
- `ResultadoQuizScreen` con las dos partes.
- La cuenta regresiva sobre la cámara (círculo liso con «Prepárate» y el número).

## Decisiones relacionadas, ya implementadas

- **Práctica de lección completa** («Empezar práctica»): recorre las letras alternando la
  indicación (imagen grande) y la cámara. Tras acertar, «Siguiente seña» abre la indicación de
  la siguiente; tras la última, «Terminar y hacer el quiz».
- **Práctica de letra suelta** (tocar una letra de la lista): indicación → cámara → «Volver a
  la lección».
- **Cuenta regresiva**: cada intento empieza solo tras 3 segundos. «No estoy listo» la cancela
  y deja «Iniciar intento» de respaldo, que arranca en el acto.

## Desviación del ERS

El RF02 decía «una imagen o clip de referencia y una descripción textual». Las descripciones se
quitaron por decisión del equipo (Kevin y Anyela) porque nunca se validaron con una fuente de
LSC. El RF02 ya está actualizado en el ERS v2.1 (sección 8), y el estado de todos los RF está en
`docs/trazabilidad_rf.md`.
