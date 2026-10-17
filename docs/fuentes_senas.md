# Fuentes de las imágenes de referencia de las señas

Este documento cumple con la restricción **RD06** del ERS: el vocabulario de señas debe
construirse a partir de fuentes de LSC verificables, documentando la fuente de cada seña.

## Origen de las imágenes

Las imágenes de referencia que muestra la app (RF02) las **creó el equipo de SignaCO** para
este proyecto. No son material de terceros, así que no requieren licencia externa.

- Formato: WebP, 512 × 512 px, fondo claro, una sola mano.
- Ubicación en el código: `app/src/main/res/drawable-nodpi/sena_<letra>.webp`.
- Asociación con la seña: por la letra, en
  `app/src/main/java/com/ufps/signaco/core/senas/data/ReferenciasEnRecursos.kt`. No hay campo
  en la base de datos: agregar una imagen es poner el archivo y añadir una línea a ese mapa.
- Una letra activa sin imagen muestra la letra grande y no entra en el quiz con imagen.

## Imágenes incluidas

| Letra | Archivo | Autoría | Seña de LSC contrastada con | Consentimiento de quien aparece (RD05) |
|---|---|---|---|---|
| A | `drawable-nodpi/sena_a.webp` | Equipo SignaCO | PENDIENTE | PENDIENTE |
| B | `drawable-nodpi/sena_b.webp` | Equipo SignaCO | PENDIENTE | PENDIENTE |
| D | `drawable-nodpi/sena_d.webp` | Equipo SignaCO | PENDIENTE | PENDIENTE |
| F | `drawable-nodpi/sena_f.webp` | Equipo SignaCO | PENDIENTE | PENDIENTE |
| G | `drawable-nodpi/sena_g.webp` | Equipo SignaCO | PENDIENTE | PENDIENTE |

## Pendiente

- **Fuente de LSC de cada seña (RD06):** indicar con qué material público y verificable de LSC
  (diccionario del INSOR, LSC70 u otro) se contrastó la forma de la mano de cada imagen.
- **Consentimiento (RD05):** confirmar que la persona cuya mano aparece en las imágenes autorizó
  su uso en la app.
- Letras activas que todavía no tienen imagen: I, J, L, O, P, Q, R, S, T, U, V, W, X, Y. Se
  agregan siguiendo el mismo procedimiento.
