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
- La hoja de contacto con todas las imágenes y su nombre de archivo está en
  `docs/hoja_de_contacto_senas.png`. Vive en docs y no en res para no engordar el APK.
- Las imágenes de G, J y S llevan una flecha que indica el movimiento de la seña. El
  clasificador evalúa la pose de la mano, no el movimiento.

## Imágenes incluidas

| Letra | Archivo | Autoría | Seña de LSC contrastada con | Consentimiento de quien aparece (RD05) |
|---|---|---|---|---|
| A | `drawable-nodpi/sena_a.webp` | Equipo SignaCO | PENDIENTE | PENDIENTE |
| B | `drawable-nodpi/sena_b.webp` | Equipo SignaCO | PENDIENTE | PENDIENTE |
| D | `drawable-nodpi/sena_d.webp` | Equipo SignaCO | PENDIENTE | PENDIENTE |
| F | `drawable-nodpi/sena_f.webp` | Equipo SignaCO | PENDIENTE | PENDIENTE |
| G | `drawable-nodpi/sena_g.webp` | Equipo SignaCO | PENDIENTE | PENDIENTE |
| I | `drawable-nodpi/sena_i.webp` | Equipo SignaCO | PENDIENTE | PENDIENTE |
| J | `drawable-nodpi/sena_j.webp` | Equipo SignaCO | PENDIENTE | PENDIENTE |
| L | `drawable-nodpi/sena_l.webp` | Equipo SignaCO | PENDIENTE | PENDIENTE |
| O | `drawable-nodpi/sena_o.webp` | Equipo SignaCO | PENDIENTE | PENDIENTE |
| P | `drawable-nodpi/sena_p.webp` | Equipo SignaCO | PENDIENTE | PENDIENTE |
| Q | `drawable-nodpi/sena_q.webp` | Equipo SignaCO | PENDIENTE | PENDIENTE |
| R | `drawable-nodpi/sena_r.webp` | Equipo SignaCO | PENDIENTE | PENDIENTE |
| S | `drawable-nodpi/sena_s.webp` | Equipo SignaCO | PENDIENTE | PENDIENTE |
| T | `drawable-nodpi/sena_t.webp` | Equipo SignaCO | PENDIENTE | PENDIENTE |
| U | `drawable-nodpi/sena_u.webp` | Equipo SignaCO | PENDIENTE | PENDIENTE |
| V | `drawable-nodpi/sena_v.webp` | Equipo SignaCO | PENDIENTE | PENDIENTE |
| W | `drawable-nodpi/sena_w.webp` | Equipo SignaCO | PENDIENTE | PENDIENTE |
| X | `drawable-nodpi/sena_x.webp` | Equipo SignaCO | PENDIENTE | PENDIENTE |
| Y | `drawable-nodpi/sena_y.webp` | Equipo SignaCO | PENDIENTE | PENDIENTE |

## Pendiente

- **Fuente de LSC de cada seña (RD06):** indicar con qué material público y verificable de LSC
  (diccionario del INSOR, LSC70 u otro) se contrastó la forma de la mano de cada imagen.
- **Consentimiento (RD05):** confirmar que la persona cuya mano aparece en las imágenes autorizó
  su uso en la app.
- Las 19 letras activas ya tienen imagen. Si el vocabulario crece, cada letra nueva se agrega
  siguiendo el mismo procedimiento.
