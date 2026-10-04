// SignaCO: rediseño "Cuaderno". Crea variables, estilos de texto, componentes y 10 pantallas nativas.
await (async () => {
const PAGE_NAME = "SignaCO: rediseño Cuaderno"
const HEX = { tinta: "#2438B8", oscura: "#1B2459", suave: "#56608A", grid: "#D6DEEA", borde: "#BAC6DA", resaltador: "#FFE14A", rojo: "#CF2A3A", papel: "#FFFFFF", fila: "#3F4770", sil: "#3B4371", camA: "#2E365F", camB: "#1F2649", rojoClaro: "#FF7C87", gris: "#8A93B5", tablero: "#E6EAF0", panel: "#141A3E" }
const VAR_NAMES = [["tinta", "Tinta"], ["oscura", "Tinta oscura"], ["suave", "Tinta suave"], ["grid", "Cuadrícula"], ["borde", "Borde"], ["resaltador", "Resaltador"], ["rojo", "Corrección"], ["papel", "Papel"]]
const STY = {
  display: ["Display", 72, "x", 100, -2], palabra: ["Palabra", 48, "x", 105, -1], titulo: ["Título", 36, "x", 110, -1],
  feedback: ["Feedback", 24, "x", 120, 0], lead: ["Entrada", 21, "r", 140, 0], seccion: ["Sección", 21, "b", 125, 0],
  seccionX: ["Sección destacada", 21, "x", 125, 0], lista: ["Palabra en lista", 21, "x", 120, 0],
  cuerpo: ["Cuerpo", 16, "r", 145, 0], cuerpoB: ["Cuerpo fuerte", 16, "b", 130, 0], cifra: ["Cifra", 16, "x", 130, 0],
  sec: ["Secundario", 14, "r", 140, 0], secB: ["Secundario fuerte", 14, "b", 140, 0],
  ley: ["Leyenda", 12, "b", 135, 0], leyX: ["Leyenda fuerte", 12, "x", 135, 0], nota: ["Nota", 12, "r", 135, 0],
  boton: ["Botón", 18, "b", 120, 0], numero: ["Número", 120, "x", 100, -3]
}
const VARS = {}, TSTY = {}, COMP = {}, BTN = {}, ICOMP = {}, PENDING = [], LOG = []
let FONT = null
const dv = (a, b) => (a === undefined || a === null ? b : a)
const rgb = h => { const n = parseInt(h.slice(1), 16); return { r: ((n >> 16) & 255) / 255, g: ((n >> 8) & 255) / 255, b: (n & 255) / 255 } }
const rgba = (h, a) => Object.assign({}, rgb(h), { a })

function solid(k, a) {
  let p = { type: "SOLID", color: rgb(HEX[k] || k), opacity: dv(a, 1) }
  if (VARS[k]) { try { p = figma.variables.setBoundVariableForPaint(p, "color", VARS[k]) } catch (e) { } }
  return p
}
function pads(p) { if (p === undefined || p === null) return [0, 0, 0, 0]; if (typeof p === "number") return [p, p, p, p]; if (p.length === 2) return [p[0], p[1], p[0], p[1]]; return p }

function F(name, o) {
  o = o || {}
  const f = o.comp ? figma.createComponent() : figma.createFrame()
  f.name = name
  f.fills = o.fill ? [o.fill.type ? o.fill : solid(o.fill, o.fa)] : []
  f.clipsContent = !!o.clip
  if (o.w != null || o.h != null) f.resize(dv(o.w, 100), dv(o.h, 100))
  if (o.dir) {
    f.layoutMode = o.dir === "v" ? "VERTICAL" : "HORIZONTAL"
    f.itemSpacing = o.gap || 0
    const p = pads(o.pad)
    f.paddingTop = p[0]; f.paddingRight = p[1]; f.paddingBottom = p[2]; f.paddingLeft = p[3]
    f.primaryAxisAlignItems = o.main || "MIN"
    f.counterAxisAlignItems = o.cross || "MIN"
    if (o.wrap) { f.layoutWrap = "WRAP"; f.counterAxisSpacing = o.wrap }
    f.layoutSizingHorizontal = o.w != null ? "FIXED" : "HUG"
    f.layoutSizingVertical = o.h != null ? "FIXED" : "HUG"
  }
  if (o.r != null) {
    if (Array.isArray(o.r)) { f.topLeftRadius = o.r[0]; f.topRightRadius = o.r[1]; f.bottomRightRadius = o.r[2]; f.bottomLeftRadius = o.r[3] }
    else f.cornerRadius = o.r
  }
  if (o.stroke) { f.strokes = [solid(o.stroke, o.sa)]; f.strokeWeight = dv(o.sw, 1); f.strokeAlign = "INSIDE" }
  if (o.bl || o.tl) {
    f.strokes = [solid(o.bl || o.tl)]; f.strokeAlign = "INSIDE"
    f.strokeTopWeight = o.tl ? 1 : 0; f.strokeBottomWeight = o.bl ? 1 : 0; f.strokeLeftWeight = 0; f.strokeRightWeight = 0
  }
  if (o.fx) f.effects = o.fx
  return f
}

function A(p, c, o) {
  o = o || {}
  p.appendChild(c)
  const al = ("layoutMode" in p) && p.layoutMode !== "NONE"
  if (o.abs) { if (al) c.layoutPositioning = "ABSOLUTE"; c.x = o.x || 0; c.y = o.y || 0 }
  else if (!al) { if (o.x != null) c.x = o.x; if (o.y != null) c.y = o.y }
  if (al && !o.abs) {
    if (o.fw) c.layoutSizingHorizontal = "FILL"
    if (o.fh) c.layoutSizingVertical = "FILL"
    if (o.grow) c.layoutGrow = 1
  }
  if (c.type === "TEXT" && o.fw) c.textAutoResize = "HEIGHT"
  return c
}

function T(chars, st, o) {
  o = o || {}
  const d = STY[st]
  const t = figma.createText()
  t.fontName = FONT[d[2]]
  t.fontSize = d[1]
  t.lineHeight = { value: d[3], unit: "PERCENT" }
  t.letterSpacing = { value: d[4], unit: "PERCENT" }
  t.characters = chars
  t.fills = [solid(o.c || "oscura", o.a)]
  if (o.align) t.textAlignHorizontal = o.align
  if (o.w) { t.textAutoResize = "HEIGHT"; t.resize(o.w, t.height) } else t.textAutoResize = "WIDTH_AND_HEIGHT"
  if (o.u) t.textDecoration = "UNDERLINE"
  t.name = o.name || chars.replace(/\n/g, " ").slice(0, 40)
  const s = TSTY[st]
  if (s && !o.ns) { try { t.textStyleId = s.id } catch (e) { PENDING.push(t.setTextStyleIdAsync(s.id)) } }
  return t
}

function R(name, w, h, o) {
  o = o || {}
  const r = figma.createRectangle()
  r.name = name; r.resize(w, h)
  r.fills = o.fill ? [solid(o.fill, o.fa)] : []
  if (o.r != null) r.cornerRadius = o.r
  if (o.stroke) { r.strokes = [solid(o.stroke, o.sa)]; r.strokeWeight = dv(o.sw, 1); r.strokeAlign = "INSIDE" }
  return r
}

function S(svg, name) { const n = figma.createNodeFromSvg(svg); n.name = name; n.fills = []; n.clipsContent = false; return n }
const SVG = (w, h, body, vb) => `<svg xmlns="http://www.w3.org/2000/svg" width="${w}" height="${h}" viewBox="${vb || `0 0 ${w} ${h}`}">${body}</svg>`

const ICON = {
  back: '<path d="M19 12H5"/><path d="M11 18l-6-6 6-6"/>',
  close: '<path d="M6 6l12 12"/><path d="M18 6L6 18"/>',
  chevron: '<path d="M9 6l6 6-6 6"/>',
  help: '<circle cx="12" cy="12" r="9"/><path d="M9.6 9.4a2.5 2.5 0 1 1 3.6 2.3c-.7.4-1.2.9-1.2 1.7v.4"/><path d="M12 17h.01"/>',
  phone: '<rect x="7" y="3" width="10" height="18" rx="2.5"/><path d="M11 17.5h2"/>',
  videoOff: '<path d="M3 3l18 18"/><path d="M15 10.5l5-2.8v8.6l-2.2-1.2"/><path d="M11 7h2a2 2 0 0 1 2 2v2"/><path d="M15 15v1a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V9a2 2 0 0 1 2-2"/>',
  wifiOff: '<path d="M12 19h.01"/><path d="M9.2 16.2a4 4 0 0 1 5.6 0"/><path d="M6.3 13.3a8 8 0 0 1 3.9-2.1"/><path d="M14.4 11.4a8 8 0 0 1 3.3 1.9"/><path d="M3.5 10.5a12 12 0 0 1 3.5-2.5"/><path d="M10.2 7.6a12 12 0 0 1 10.3 2.9"/><path d="M3 3l18 18"/>',
  check: '<path d="M5 12.5l4.5 4.5L19 7.5"/>',
  book: '<path d="M3 6.5c2.8-1.6 5.8-1.6 9 .6 3.2-2.2 6.2-2.2 9-.6V19c-2.8-1.4-5.8-1.4-9 .6-3.2-2-6.2-2-9-.6z"/><path d="M12 7.1v12.5"/>',
  dict: '<path d="M5 5a2 2 0 0 1 2-2h12v15H7a2 2 0 0 0-2 2z"/><path d="M5 20a2 2 0 0 0 2 1h12"/><path d="M9 8h6"/><path d="M9 12h4"/>',
  grid: '<rect x="4" y="4" width="7" height="7" rx="1.2"/><rect x="13" y="4" width="7" height="7" rx="1.2"/><rect x="4" y="13" width="7" height="7" rx="1.2"/><rect x="13" y="13" width="7" height="7" rx="1.2"/>',
  trash: '<path d="M4 7h16"/><path d="M10 11v6"/><path d="M14 11v6"/><path d="M5.5 7l1 12a2 2 0 0 0 2 2h7a2 2 0 0 0 2-2l1-12"/><path d="M9 7V4.5h6V7"/>',
  calendar: '<rect x="4" y="5" width="16" height="15" rx="2"/><path d="M4 10h16"/><path d="M9 3v4"/><path d="M15 3v4"/><path d="M9.3 15l2 2 3.6-3.6"/>',
  retry: '<path d="M19.5 12a7.5 7.5 0 1 1-2.2-5.3"/><path d="M19.5 4.5v4h-4"/>',
  checkCircle: '<circle cx="12" cy="12" r="9"/><path d="M8.2 12.4l2.6 2.6 5-5.2"/>',
  xCircle: '<circle cx="12" cy="12" r="9"/><path d="M9.2 9.2l5.6 5.6"/><path d="M14.8 9.2l-5.6 5.6"/>',
  play: '<path d="M8.5 5.8v12.4a.6.6 0 0 0 .9.5l9.8-6.2a.6.6 0 0 0 0-1L9.4 5.3a.6.6 0 0 0-.9.5z"/>',
  record: '<circle cx="12" cy="12" r="6.5"/>',
  stop: '<rect x="7" y="7" width="10" height="10" rx="2"/>'
}
const FILLED = { play: 1, record: 1, stop: 1 }
function iconSvg(name, size, hex, sw) {
  if (FILLED[name]) return `<svg xmlns="http://www.w3.org/2000/svg" width="${size}" height="${size}" viewBox="0 0 24 24" fill="${hex}">${ICON[name]}</svg>`
  return `<svg xmlns="http://www.w3.org/2000/svg" width="${size}" height="${size}" viewBox="0 0 24 24" fill="none" stroke="${hex}" stroke-width="${dv(sw, 2)}" stroke-linecap="round" stroke-linejoin="round">${ICON[name]}</svg>`
}
function I(name, size, k, sw) { return S(iconSvg(name, size || 24, HEX[k || "oscura"] || k, sw), "Ícono " + name) }
function recolor(node, k) {
  const list = node.findAll(n => n.type === "VECTOR" || n.type === "ELLIPSE" || n.type === "RECTANGLE" || n.type === "BOOLEAN_OPERATION")
  for (const n of list) {
    if (Array.isArray(n.strokes) && n.strokes.length) n.strokes = [solid(k)]
    if (Array.isArray(n.fills) && n.fills.length) n.fills = [solid(k)]
  }
}

function gridSvg(w, h, step) {
  step = step || 16
  let d = ""
  for (let x = step; x < w; x += step) d += `M${x + 0.5} 0V${h}`
  for (let y = step; y < h; y += step) d += `M0 ${y + 0.5}H${w}`
  return SVG(w, h, `<path d="${d}" stroke="${HEX.grid}" stroke-width="1" fill="none"/>`)
}
const HAND = [[0, 0], [-.13, -.10], [-.25, -.21], [-.34, -.32], [-.42, -.42], [-.14, -.42], [-.17, -.60], [-.19, -.72], [-.20, -.83], [-.02, -.45], [-.02, -.65], [-.02, -.78], [-.02, -.90], [.09, -.42], [.12, -.60], [.14, -.72], [.15, -.82], [.19, -.36], [.25, -.49], [.28, -.58], [.31, -.66]]
const EDGES = [[0, 1], [1, 2], [2, 3], [3, 4], [0, 5], [5, 6], [6, 7], [7, 8], [5, 9], [9, 10], [10, 11], [11, 12], [9, 13], [13, 14], [14, 15], [15, 16], [13, 17], [17, 18], [18, 19], [19, 20], [0, 17]]
function handBody(cx, cy, size, rot, line, lineOp, dot, dotStroke, lw, r, ds) {
  const a = rot * Math.PI / 180
  const P = HAND.map(q => [cx + (q[0] * Math.cos(a) - q[1] * Math.sin(a)) * size, cy + (q[0] * Math.sin(a) + q[1] * Math.cos(a)) * size])
  const d = EDGES.map(e => `M${P[e[0]][0].toFixed(1)} ${P[e[0]][1].toFixed(1)}L${P[e[1]][0].toFixed(1)} ${P[e[1]][1].toFixed(1)}`).join("")
  let s = `<path d="${d}" stroke="${line}" stroke-opacity="${lineOp}" stroke-width="${dv(lw, 2.5)}" stroke-linecap="round" fill="none"/>`
  for (const p of P) s += `<circle cx="${p[0].toFixed(1)}" cy="${p[1].toFixed(1)}" r="${dv(r, 4.5)}" fill="${dot}" stroke="${dotStroke}" stroke-width="${dv(ds, 1.5)}"/>`
  return s
}

function tile(name, w, h, step) {
  const t = F(name, { w, h, fill: "papel", stroke: "borde", r: 8, clip: true })
  A(t, S(gridSvg(w, h, step), "Cuadrícula"), { x: 0, y: 0 })
  return t
}
function stamp(size, rot) {
  const g = F("Sello", { w: size, h: size })
  A(g, S(SVG(size, size, `<circle cx="48" cy="48" r="44" fill="none" stroke="${HEX.tinta}" stroke-width="3.5"/><circle cx="48" cy="48" r="37" fill="none" stroke="${HEX.tinta}" stroke-width="1.5"/><path d="M34 42l9 9 19-19" stroke="${HEX.tinta}" stroke-width="5.5" fill="none" stroke-linecap="round" stroke-linejoin="round"/>`, "0 0 96 96"), "Aros"), { x: 0, y: 0 })
  const t = T("¡Bien!", "leyX", { c: "tinta", ns: true })
  t.fontSize = 14
  A(g, t, { x: Math.round((size - t.width) / 2), y: Math.round(size * 0.695 - t.height / 2) })
  g.rotation = rot
  return g
}
function iconBtn(name) { const b = F("Botón de ícono", { dir: "h", w: 48, h: 48, main: "CENTER", cross: "CENTER" }); A(b, I(name)); return b }
function meter(w, pct, k, dark) {
  const m = F("Medidor", { w, h: 16 })
  A(m, R("Pista", w, 8, { r: 4, fill: dark ? "papel" : "grid", fa: dark ? 0.22 : 1 }), { x: 0, y: 4 })
  A(m, R("Nivel", Math.round(w * pct / 100), 8, { r: 4, fill: k }), { x: 0, y: 4 })
  A(m, R("Umbral 75 %", 2, 16, { r: 1, fill: dark ? "papel" : "oscura" }), { x: Math.round(w * 0.75) - 1, y: 0 })
  return m
}
function legendItem(p, shape, label) { const it = A(p, F(label, { dir: "h", gap: 6, cross: "CENTER" })); A(it, shape); A(it, T(label, "ley", { c: "suave" })) }

// ---------- componentes ----------
function makeComponents(sec) {
  const st = F("Barra de estado", { comp: true, dir: "h", w: 360, h: 32, pad: [0, 20, 0, 24], main: "SPACE_BETWEEN", cross: "CENTER", fill: "papel" })
  A(sec, st, { x: 80, y: 120 })
  A(st, T("9:30", "secB", { name: "Hora" }))
  const ics = A(st, F("Íconos", { dir: "h", gap: 6, cross: "CENTER" }))
  const o = HEX.oscura
  A(ics, S(SVG(16, 16, `<path d="M12 20.5L1.5 8.3C4.4 5.6 8 4 12 4s7.6 1.6 10.5 4.3z" fill="${o}"/>`, "0 0 24 24"), "Wifi"))
  A(ics, S(SVG(16, 16, `<path d="M2.5 21.5h19v-19z" fill="${o}"/>`, "0 0 24 24"), "Señal"))
  A(ics, S(SVG(16, 16, `<rect x="9.5" y="1.5" width="5" height="2.5" rx="1" fill="${o}"/><rect x="6.5" y="3.5" width="11" height="19" rx="2.5" fill="${o}"/>`, "0 0 24 24"), "Batería"))
  COMP.status = st

  const ge = F("Barra de gestos", { comp: true, dir: "h", w: 360, h: 24, main: "CENTER", cross: "CENTER", fill: "papel" })
  A(sec, ge, { x: 80, y: 184 })
  A(ge, R("Asa", 108, 4, { fill: "oscura", fa: 0.35, r: 2 }))
  COMP.gesture = ge

  let x = 80
  for (const [k, label] of [["record", "Grabar"], ["stop", "Detener"], ["retry", "Reintentar"]]) {
    const c = F("Ícono/" + label, { comp: true, w: 24, h: 24 })
    A(sec, c, { x, y: 240 }); x += 48
    A(c, S(iconSvg(k, 24, HEX.oscura, 2), "Trazo"), { x: 0, y: 0 })
    ICOMP[k] = c
  }

  let y = 300
  for (const [k, v] of [["primario", { fill: "tinta", fg: "papel", h: 56 }], ["secundario", { fill: "papel", stroke: "tinta", fg: "tinta", h: 56 }], ["texto", { fg: "tinta", h: 48 }]]) {
    const c = F("Botón/" + k.charAt(0).toUpperCase() + k.slice(1), { comp: true, dir: "h", w: 320, h: v.h, main: "CENTER", cross: "CENTER", gap: 8, r: v.h / 2, fill: v.fill, stroke: v.stroke, sw: 2 })
    A(sec, c, { x: 80, y }); y += v.h + 24
    const ic = ICOMP.record.createInstance(); ic.name = "Ícono"
    A(c, ic); recolor(ic, v.fg); ic.visible = false
    A(c, T("Botón", "boton", { c: v.fg, name: "Etiqueta" }))
    BTN[k] = c
  }

  const variants = []
  let vy = 560
  for (const act of ["Aprender", "Diccionario", "Progreso"]) {
    const c = F("Activo=" + act, { comp: true, dir: "h", w: 360, h: 80, pad: [12, 8, 0, 8], fill: "papel", tl: "grid" })
    A(sec, c, { x: 80, y: vy }); vy += 104
    for (const [ic, lab] of [["book", "Aprender"], ["dict", "Diccionario"], ["grid", "Progreso"]]) {
      const on = lab === act
      const it = A(c, F(lab, { dir: "v", gap: 4, cross: "CENTER" }), { fw: true })
      const ind = A(it, F("Indicador", { dir: "h", w: 64, h: 32, r: 16, main: "CENTER", cross: "CENTER", fill: on ? "resaltador" : null }))
      A(ind, I(ic, 24, on ? "oscura" : "suave"))
      A(it, T(lab, "ley", { c: on ? "oscura" : "suave" }))
    }
    variants.push(c)
  }
  const set = figma.combineAsVariants(variants, sec)
  set.name = "Navegación"
  set.layoutMode = "VERTICAL"; set.itemSpacing = 24
  set.paddingTop = 24; set.paddingBottom = 24; set.paddingLeft = 24; set.paddingRight = 24
  set.layoutSizingHorizontal = "HUG"; set.layoutSizingVertical = "HUG"
  set.x = 80; set.y = 560
  COMP.nav = set
}
function btn(kind, label, icon) {
  const i = BTN[kind].createInstance()
  i.findOne(n => n.name === "Etiqueta").characters = label
  if (icon) {
    const ic = i.findOne(n => n.name === "Ícono")
    ic.visible = true
    ic.swapComponent(ICOMP[icon])
    recolor(ic, kind === "primario" ? "papel" : "tinta")
  }
  return i
}

// ---------- pantallas ----------
function screen(name) { return F(name, { dir: "v", w: 360, h: 800, fill: "papel", clip: true }) }
const status = s => A(s, COMP.status.createInstance(), { fw: true })
const gesture = s => A(s, COMP.gesture.createInstance(), { fw: true })
const nav = (s, act) => A(s, COMP.nav.children.find(c => c.name === "Activo=" + act).createInstance(), { fw: true })
function actions(s) { return A(s, F("Acciones", { dir: "v", gap: 8, pad: [12, 20, 8, 20] }), { fw: true }) }
function topBar(s, left, title, right) {
  const b = A(s, F("Barra superior", { dir: "h", h: 56, pad: [0, 8], main: title || right ? "SPACE_BETWEEN" : "MIN", cross: "CENTER" }), { fw: true })
  A(b, iconBtn(left))
  if (title) A(b, T(title, "cuerpoB"))
  if (right === "empty") A(b, F("Espacio", { w: 48, h: 48 })); else if (right) A(b, iconBtn(right))
  return b
}

function sPermiso() {
  const s = screen("01 Permiso de cámara"); status(s)
  const c = A(s, F("Contenido", { dir: "v", gap: 28, pad: [16, 20, 0, 20] }), { fw: true, fh: true })
  const il = A(c, tile("Ilustración: 21 puntos", 320, 184))
  A(il, S(SVG(320, 184, handBody(222, 168, 150, 6, HEX.tinta, 1, "#FFFFFF", HEX.tinta, 2.5, 5, 2)), "Mano: 21 puntos"), { x: 0, y: 0 })
  const cap = T("21 puntos. Es todo lo que la app lee de tu mano.", "sec", { w: 132, name: "Pie de ilustración", ns: true })
  cap.setRangeFontName(0, 10, FONT.b)
  A(il, cap, { x: 16, y: Math.round(184 - 14 - cap.height) })
  const b = A(c, F("Texto", { dir: "v", gap: 24 }), { fw: true })
  const intro = A(b, F("Intro", { dir: "v", gap: 12 }), { fw: true })
  A(intro, T("Practica frente a la cámara", "titulo"), { fw: true })
  A(intro, T("Para corregir tus señas, SignaCO necesita ver tus manos mientras practicas.", "cuerpo", { c: "suave" }), { fw: true })
  const facts = A(b, F("Garantías", { dir: "v", gap: 16 }), { fw: true })
  for (const [ic, t1, t2] of [["phone", "Todo se procesa en tu teléfono", "El video nunca sale del dispositivo."], ["videoOff", "No se guarda ningún video", "Solo se leen los puntos de tu mano, cuadro a cuadro."], ["wifiOff", "Funciona sin conexión", "No necesitas internet para practicar."]]) {
    const row = A(facts, F(t1, { dir: "h", gap: 14 }), { fw: true })
    A(row, I(ic, 24, "tinta"))
    const tx = A(row, F("Texto", { dir: "v", gap: 2 }), { fw: true })
    A(tx, T(t1, "cuerpoB"), { fw: true })
    A(tx, T(t2, "sec", { c: "suave" }), { fw: true })
  }
  const act = actions(s)
  A(act, btn("primario", "Permitir cámara"), { fw: true })
  A(act, btn("texto", "Ahora no"), { fw: true })
  A(act, T("Puedes cambiar el permiso cuando quieras en los ajustes de Android.", "nota", { c: "suave", align: "CENTER" }), { fw: true })
  gesture(s)
  return s
}

function sCatalogo() {
  const s = screen("02 Catálogo de lecciones"); status(s)
  const c = A(s, F("Contenido", { dir: "v", gap: 28, pad: [16, 0, 0, 0] }), { fw: true, fh: true })
  const head = A(c, F("Encabezado", { dir: "v", gap: 4, pad: [0, 20] }), { fw: true })
  const tr = A(head, F("Título y racha", { dir: "h", main: "SPACE_BETWEEN", cross: "CENTER" }), { fw: true })
  A(tr, T("Aprende LSC", "titulo"))
  const chip = A(tr, F("Racha", { dir: "h", gap: 6, h: 32, pad: [0, 12], cross: "CENTER", r: 16, stroke: "borde", sw: 1.5 }))
  A(chip, I("calendar", 18, "tinta")); A(chip, T("5 días", "secB", { c: "tinta" }))
  A(head, T("9 de 20 señas dominadas", "cuerpo", { c: "suave" }), { fw: true })
  const body = A(c, F("Lecciones", { dir: "v", gap: 8, pad: [0, 12] }), { fw: true })
  const tw = A(body, F("Título", { dir: "h", pad: [0, 8] }), { fw: true })
  A(tw, T("Lecciones", "seccion"))
  const list = A(body, F("Lista", { dir: "v" }), { fw: true })
  const L = [["Saludos básicos", "Hola, buenos días, buenas tardes, adiós", "deee", true], ["Cortesía", "Por favor, gracias, perdón, de nada", "dddd"], ["Necesidades elementales", "Agua, comida, baño, ayuda", "dddd"], ["Ubicación", "Aquí, casa, calle, lejos", "eeee"], ["Emergencia", "Emergencia, dolor, hospital, policía", "eeee"]]
  for (const [title, words, st, cur] of L) {
    const r = A(list, F(title, { dir: "h", gap: 14, pad: [12, 8], cross: "CENTER", fill: cur ? "resaltador" : null, r: cur ? 6 : null, bl: cur ? null : "grid" }), { fw: true })
    const sq = A(r, F("Progreso", { dir: "h", gap: 4 }))
    for (const ch of st) A(sq, R(ch === "d" ? "Dominada" : "Por aprender", 14, 14, ch === "d" ? { fill: "tinta", r: 3 } : { fill: "papel", r: 3, stroke: "borde", sw: 1.5 }))
    const tx = A(r, F("Texto", { dir: "v", gap: 2 }), { fw: true })
    A(tx, T(title, "cuerpoB"), { fw: true })
    A(tx, T(words, "sec", { c: cur ? "fila" : "suave" }), { fw: true })
    if (cur) { const go = A(r, F("Seguir", { dir: "h", gap: 2, cross: "CENTER" })); A(go, T("Seguir", "secB", { c: "tinta" })); A(go, I("chevron", 20, "tinta")) }
    else A(r, I("chevron", 20, "suave"))
  }
  const lg = A(body, F("Leyenda", { dir: "h", gap: 16, pad: [6, 8, 0, 8], cross: "CENTER" }), { fw: true })
  legendItem(lg, R("Dominada", 12, 12, { fill: "tinta", r: 3 }), "Dominada")
  legendItem(lg, R("Por aprender", 12, 12, { fill: "papel", r: 3, stroke: "borde", sw: 1.5 }), "Por aprender")
  legendItem(lg, R("En curso", 16, 10, { fill: "resaltador", r: 2 }), "Lección en curso")
  nav(s, "Aprender"); gesture(s)
  return s
}

function sLeccion() {
  const s = screen("03 Lección"); status(s)
  topBar(s, "back")
  const c = A(s, F("Contenido", { dir: "v", gap: 16, pad: [0, 20] }), { fw: true, fh: true })
  const intro = A(c, F("Intro", { dir: "v", gap: 8 }), { fw: true })
  A(intro, T("Saludos básicos", "titulo"), { fw: true })
  A(intro, T("Mira cada seña antes de practicarla con la cámara. Son 4 y ya dominas 1.", "cuerpo", { c: "suave" }), { fw: true })
  const list = A(c, F("Señas", { dir: "v" }), { fw: true })
  const SG = [["Hola", "Mano abierta junto a la sien, palma al frente. Muévela hacia afuera en un arco corto.", true], ["Buenos días", "Yemas de los dedos en el mentón; baja la mano abierta hasta la palma contraria."], ["Buenas tardes", "Antebrazo inclinado sobre el dorso de la otra mano, como un sol que baja."], ["Adiós", "Mano abierta a la altura del hombro; abre y cierra los dedos dos veces."]]
  SG.forEach(([w, d, dom], i) => {
    const r = A(list, F(w, { dir: "h", gap: 16, pad: [14, 0], bl: i < 3 ? "grid" : null }), { fw: true })
    const tl = A(r, tile("Clip de referencia", 72, 72, 12))
    A(tl, T(String(i + 1), "leyX", { c: "tinta" }), { x: 7, y: 4 })
    A(tl, I("play", 28, "tinta"), { x: 22, y: 22 })
    const tx = A(r, F("Texto", { dir: "v", gap: 4 }), { fw: true })
    const hd = A(tx, F("Encabezado", { dir: "h", gap: 10, cross: "CENTER" }), { fw: true })
    A(hd, T(w, "lista"))
    if (dom) { const bd = A(hd, F("Dominada", { dir: "h", gap: 3, cross: "CENTER" })); A(bd, I("check", 16, "tinta", 2.5)); A(bd, T("Dominada", "ley", { c: "tinta" })) }
    A(tx, T(d, "sec", { c: "suave" }), { fw: true })
  })
  A(actions(s), btn("primario", "Empezar práctica"), { fw: true })
  gesture(s)
  return s
}

function sPractica(state, label, idx) {
  const err = state === "error", word = err ? "Buenos días" : "Hola", n = err ? 2 : 1
  const s = screen("0" + idx + " Práctica: " + label); status(s)
  topBar(s, "close", "Seña " + n + " de 4", "help")
  const segs = A(s, F("Avance de la lección", { dir: "h", gap: 6, pad: [0, 20, 14, 20] }), { fw: true })
  const code = { idle: "yeee", buscando: "yeee", reconociendo: "yeee", acierto: "deee", error: "dyee" }[state]
  for (const ch of code) A(segs, R(ch === "d" ? "Hecha" : ch === "y" ? "Actual" : "Pendiente", 10, 6, { r: 3, fill: ch === "d" ? "tinta" : ch === "y" ? "resaltador" : "grid", stroke: ch === "y" ? "oscura" : null, sa: 0.18 }), { grow: true })
  const pr = A(s, F("Práctica", { dir: "v", gap: 12, h: 592, pad: [0, 20] }), { fw: true })
  const wt = A(pr, tile("Palabra", 320, 104))
  const wb = A(wt, F("Texto", { dir: "v", gap: 4 }), { x: 20, y: 14 })
  A(wb, T(word, "palabra", { c: "tinta" })); A(wb, T("Haz esta seña frente a la cámara", "sec", { c: "suave" }))
  const grad = { type: "GRADIENT_LINEAR", gradientTransform: [[0, 1, 0], [-1, 0, 1]], gradientStops: [{ position: 0, color: rgba(HEX.camA, 1) }, { position: 1, color: rgba(HEX.camB, 1) }] }
  const cam = A(pr, F("Cámara", { w: 320, h: 476, r: 20, clip: true, fill: grad }))
  const sil = HEX.sil
  A(cam, S(SVG(320, 476, `<circle cx="156" cy="206" r="58" fill="${sil}"/><rect x="134" y="250" width="44" height="70" fill="${sil}"/><path d="M-10 476L-10 424Q-4 350 70 334L128 318H184L242 334Q316 350 322 424L322 476Z" fill="${sil}"/>`), "Silueta (vista de cámara)"), { x: 0, y: 0 })
  const fs = state === "buscando" ? [HEX.resaltador, 1] : state === "idle" ? ["#FFFFFF", 0.55] : ["#FFFFFF", 0.35]
  A(cam, S(SVG(320, 476, `<rect x="24" y="48" width="272" height="392" rx="20" fill="none" stroke="${fs[0]}" stroke-opacity="${fs[1]}" stroke-width="2" stroke-dasharray="10 8"/>`), "Espacio de señas"), { x: 0, y: 0 })
  if (state === "reconociendo") A(cam, S(SVG(320, 476, handBody(252, 312, 156, 8, "#FFFFFF", 1, HEX.resaltador, HEX.oscura)), "Puntos de la mano"), { x: 0, y: 0 })
  if (state === "acierto") A(cam, S(SVG(320, 476, handBody(252, 312, 156, 8, "#FFFFFF", 1, "#FFFFFF", HEX.tinta)), "Puntos de la mano"), { x: 0, y: 0 })
  if (err) A(cam, S(SVG(320, 476, handBody(318, 300, 156, -10, "#FFFFFF", 0.85, HEX.rojoClaro, HEX.oscura)), "Puntos de la mano (fuera del recuadro)"), { x: 0, y: 0 })
  const P = { idle: ["papel", "gris", "Cámara lista", "oscura"], buscando: ["papel", "resaltador", "Buscando tu mano", "oscura"], reconociendo: ["resaltador", "oscura", "Reconociendo…", "oscura"], acierto: ["tinta", null, "Seña confirmada", "papel"], error: ["rojo", null, "Sin coincidencia", "papel"] }[state]
  const pill = A(cam, F("Estado", { dir: "h", gap: 8, h: 32, pad: [0, 14, 0, 12], r: 16, cross: "CENTER", fill: P[0] }), { x: 14, y: 14 })
  if (state === "acierto") A(pill, I("check", 16, "papel", 2.5))
  else if (err) A(pill, I("close", 16, "papel", 2.5))
  else {
    const d = figma.createEllipse(); d.name = "Punto"; d.resize(10, 10); d.fills = [solid(P[1])]
    if (state === "buscando") { d.strokes = [solid("oscura", 0.25)]; d.strokeWeight = 2; d.strokeAlign = "OUTSIDE" }
    A(pill, d)
  }
  A(pill, T(P[2], "secB", { c: P[3] }))
  if (state === "reconociendo") {
    const lv = A(cam, F("Coincidencia en vivo", { dir: "v", gap: 8, w: 292, pad: [12, 14], r: 12, fill: "panel", fa: 0.78 }), { x: 14, y: 300 })
    const r1 = A(lv, F("Fila", { dir: "h", main: "SPACE_BETWEEN", cross: "CENTER" }), { fw: true })
    A(r1, T("Coincidencia", "secB", { c: "papel" })); A(r1, T("68 %", "cifra", { c: "papel" }))
    A(lv, meter(264, 68, "resaltador", true))
    A(lv, T("Se confirma desde el 75 %", "nota", { c: "papel", a: 0.8 }))
    lv.y = Math.round(476 - 14 - lv.height)
  }
  if (state === "idle" || state === "buscando" || state === "reconociendo") {
    A(actions(s), state === "idle" ? btn("primario", "Iniciar intento", "record") : btn("secundario", "Detener", "stop"), { fw: true })
    gesture(s)
    return s
  }
  const shadow = { type: "DROP_SHADOW", color: rgba(HEX.oscura, 0.22), offset: { x: 0, y: -10 }, radius: 28, spread: 0, visible: true, blendMode: "NORMAL" }
  const sh = A(s, F(err ? "Hoja: sin coincidencia" : "Hoja: acierto", { dir: "v", gap: 14, w: 360, pad: [24, 20, 0, 20], fill: "papel", r: [24, 24, 0, 0], fx: [shadow] }), { abs: true, x: 0, y: 500 })
  if (!err) {
    const rw = A(sh, F("Resultado", { dir: "h", gap: 16, cross: "CENTER" }), { fw: true })
    A(rw, stamp(88, 10))
    const tx = A(rw, F("Texto", { dir: "v", gap: 4 }), { fw: true })
    A(tx, T("¡Acertaste!", "feedback"), { fw: true })
    A(tx, T("Hola, con 93 % de coincidencia.", "cuerpo", { c: "suave" }), { fw: true })
    A(sh, btn("primario", "Siguiente seña"), { fw: true })
  } else {
    const tt = A(sh, F("Título", { dir: "h", gap: 10, cross: "CENTER" }), { fw: true })
    A(tt, I("xCircle", 28, "rojo", 2.2)); A(tt, T("No reconocimos la seña", "feedback"))
    A(sh, T("Acerca la mano al recuadro y mantén la seña quieta un segundo.", "cuerpo"), { fw: true })
    const mb = A(sh, F("Coincidencia", { dir: "v", gap: 8, pad: [4, 0, 2, 0] }), { fw: true })
    A(mb, meter(320, 41, "rojo", false))
    A(mb, T("41 % de coincidencia. Se necesita 75 %.", "sec", { c: "suave" }), { fw: true })
    A(sh, btn("primario", "Reintentar", "retry"), { fw: true })
  }
  A(sh, COMP.gesture.createInstance(), { fw: true })
  sh.y = Math.round(800 - sh.height)
  return s
}

function sQuiz() {
  const s = screen("09 Resultado del quiz"); status(s)
  topBar(s, "close", "Quiz de saludos básicos", "empty")
  const c = A(s, F("Contenido", { dir: "v", gap: 20, pad: [0, 20] }), { fw: true, fh: true })
  const tl = A(c, tile("Calificación", 320, 160))
  const num = A(tl, T("3", "numero", { c: "tinta" }), { x: 20, y: 10 })
  const of = T("de 4\naciertos", "seccion")
  A(tl, of, { x: Math.round(20 + num.width + 10), y: Math.round(10 + num.height - 18 - of.height) })
  A(tl, stamp(92, -8), { x: 206, y: 34 })
  const b = A(c, F("Detalle", { dir: "v", gap: 16 }), { fw: true })
  A(b, T("«Buenos\u00A0días» pasó a dominada. «Buenas\u00A0tardes» necesita otro intento.", "cuerpo"), { fw: true })
  const list = A(b, F("Respuestas", { dir: "v" }), { fw: true })
  const RS = [["checkCircle", "tinta", "Hola", () => T("Acertada", "sec", { c: "suave" })], ["checkCircle", "tinta", "Buenos días", () => T("Ahora dominada", "secB", { c: "tinta" })], ["checkCircle", "tinta", "Adiós", () => T("Acertada", "sec", { c: "suave" })], ["xCircle", "rojo", "Buenas tardes", () => T("Repasar", "secB", { c: "tinta", u: true })]]
  RS.forEach(([ic, k, w, right], i) => {
    const r = A(list, F(w, { dir: "h", gap: 12, h: 52, cross: "CENTER", bl: i < 3 ? "grid" : null }), { fw: true })
    A(r, I(ic, 24, k)); A(r, T(w, "cuerpoB"), { fw: true }); A(r, right())
  })
  const act = actions(s)
  A(act, btn("primario", "Repetir el quiz"), { fw: true })
  A(act, btn("secundario", "Ver mi progreso"), { fw: true })
  gesture(s)
  return s
}

function sProgreso() {
  const s = screen("10 Progreso"); status(s)
  const c = A(s, F("Contenido", { dir: "v", gap: 20, pad: [16, 20, 0, 20] }), { fw: true, fh: true })
  const hd = A(c, F("Encabezado", { dir: "v", gap: 4 }), { fw: true })
  A(hd, T("Tu progreso", "titulo"), { fw: true })
  A(hd, T("Se guarda en este teléfono. No se envía a ningún servidor.", "sec", { c: "suave" }), { fw: true })
  const panel = A(c, tile("Cuaderno de progreso", 320, 352))
  const gp = A(panel, F("Contenido", { dir: "v", gap: 16, w: 288 }), { x: 16, y: 16 })
  const gh = A(gp, F("Resumen", { dir: "v", gap: 2, h: 48, main: "CENTER" }), { fw: true })
  A(gh, T("10 de 20 señas dominadas", "seccionX"), { fw: true })
  A(gh, T("2 de 5 lecciones completas", "sec", { c: "suave" }), { fw: true })
  const rows = A(gp, F("Lecciones", { dir: "v", gap: 16 }), { fw: true })
  for (const [cat, st] of [["Saludos", "ddpp"], ["Cortesía", "dddd"], ["Necesidades", "dddd"], ["Ubicación", "eeee"], ["Emergencia", "eeee"]]) {
    const r = A(rows, F(cat, { dir: "h", gap: 16, h: 32, cross: "CENTER" }), { fw: true })
    A(r, T(cat, "secB", { w: 96 }))
    const sq = A(r, F("Señas", { dir: "h", gap: 16 }))
    for (const ch of st) A(sq, R(ch === "d" ? "Dominada" : ch === "p" ? "Practicada" : "Por aprender", 32, 32, ch === "d" ? { fill: "tinta", r: 3 } : ch === "p" ? { fill: "resaltador", r: 3, stroke: "oscura", sa: 0.22, sw: 1.5 } : { r: 3, stroke: "borde", sw: 1.5 }))
  }
  const lg = A(gp, F("Leyenda", { dir: "h", gap: 14, h: 16, cross: "CENTER" }), { fw: true })
  legendItem(lg, R("Dominada", 12, 12, { fill: "tinta", r: 3 }), "Dominada")
  legendItem(lg, R("Practicada", 12, 12, { fill: "resaltador", r: 3, stroke: "oscura", sa: 0.22, sw: 1 }), "Practicada")
  legendItem(lg, R("Por aprender", 12, 12, { r: 3, stroke: "borde", sw: 1.5 }), "Por aprender")
  const stb = A(c, F("Racha", { dir: "v", gap: 10 }), { fw: true })
  const s1 = A(stb, F("Texto", { dir: "v", gap: 2 }), { fw: true })
  A(s1, T("5 días seguidos", "cuerpoB"), { fw: true })
  A(s1, T("Tu mejor racha: 9 días", "sec", { c: "suave" }), { fw: true })
  const wk = A(stb, F("Semana", { dir: "h", gap: 8 }))
  for (const [d, on] of [["L", 1], ["M", 1], ["M", 1], ["J", 1], ["V", 1], ["S", 0], ["D", 0]]) {
    const ci = A(wk, F(d, { dir: "h", w: 32, h: 32, r: 16, main: "CENTER", cross: "CENTER", fill: on ? "tinta" : null, stroke: on ? null : "borde", sw: 1.5 }))
    A(ci, T(d, "ley", { c: on ? "papel" : "suave" }))
  }
  const dz = A(c, F("Borrar", { dir: "v" }), { fw: true })
  const dr = A(dz, F("Botón borrar", { dir: "h", gap: 8, h: 40, cross: "CENTER" }))
  A(dr, I("trash", 20, "rojo")); A(dr, T("Borrar mi progreso", "cuerpoB", { c: "rojo" }))
  A(dz, T("Te pediremos confirmación. El borrado es definitivo.", "nota", { c: "suave" }), { fw: true })
  nav(s, "Progreso"); gesture(s)
  return s
}

function intro() {
  const f = F("Guía visual", { dir: "h", gap: 56, pad: 64, fill: "papel", r: 24 })
  const l = A(f, F("Concepto", { dir: "v", gap: 16, w: 400 }))
  A(l, T("SignaCO", "display", { c: "tinta" }), { fw: true })
  A(l, T("Dirección visual «Cuaderno». Tinta azul para actuar, resaltador para lo que estás aprendiendo ahora y rojo solo para corregir. La cuadrícula del cuaderno es también el mapa de tu progreso: 20 cuadros, 20 señas.", "lead"), { fw: true })
  A(l, T("Pantallas de 360 × 800 dp, el tamaño base de Android. Tipografía: Atkinson Hyperlegible Next, diseñada por el Braille Institute para que las letras parecidas no se confundan.", "sec", { c: "suave" }), { fw: true })
  const sw = A(f, F("Colores", { dir: "h", gap: 16, wrap: 24, w: 376 }))
  for (const [k, name, role] of [["tinta", "Tinta", "Acciones, lo dominado, sellos"], ["oscura", "Tinta oscura", "Texto"], ["resaltador", "Resaltador", "Lo que aprendes ahora"], ["rojo", "Corrección", "Solo errores"], ["grid", "Cuadrícula", "Líneas y divisores"], ["papel", "Papel", "Fondo"]]) {
    const it = A(sw, F(name, { dir: "v", gap: 4, w: 180 }))
    A(it, R(name, 180, 64, { fill: k, r: 8, stroke: "oscura", sa: 0.14 }))
    A(it, T(name, "secB"), { fw: true })
    A(it, T(HEX[k], "nota", { c: "suave" }), { fw: true })
    A(it, T(role, "nota", { c: "suave" }), { fw: true })
  }
  const ty = A(f, F("Tipografía", { dir: "v", gap: 14, w: 440 }))
  for (const [name, spec, ex, st] of [["Palabra", "48 / 800", "Hola", "palabra"], ["Título", "36 / 800", "Tu progreso", "titulo"], ["Feedback", "24 / 800", "¡Acertaste!", "feedback"], ["Sección", "21 / 700", "Lecciones", "seccion"], ["Cuerpo", "16 / 400", "Mira cada seña antes de practicarla.", "cuerpo"], ["Secundario", "14 / 400", "Se guarda en este teléfono.", "sec"], ["Leyenda", "12 / 700", "Por aprender", "ley"]]) {
    const r = A(ty, F(name, { dir: "h", gap: 16, cross: "CENTER" }), { fw: true })
    const sp = A(r, F("Especificación", { dir: "v", w: 96 }))
    A(sp, T(name, "secB"), { fw: true }); A(sp, T(spec, "nota", { c: "suave" }), { fw: true })
    A(r, T(ex, st, { c: st === "palabra" ? "tinta" : "oscura" }))
  }
  return f
}

// ---------- ejecución ----------
function section(name, x, y, w, h) { const s = figma.createSection(); s.name = name; s.x = x; s.y = y; s.resizeWithoutConstraints(w, h); return s }
function safe(name, fn) { try { const r = fn(); LOG.push("ok " + name); return r } catch (e) { LOG.push("ERROR " + name + ": " + (e && e.message ? e.message : e)); return null } }
function place(sec, frames, notes) {
  frames.forEach((fr, i) => {
    if (!fr) return
    A(sec, fr, { x: 80 + i * 440, y: 120 })
    if (notes[i]) A(sec, T(notes[i], "sec", { c: "suave", w: 360 }), { x: 80 + i * 440, y: 940 })
  })
}
async function pickFonts() {
  const list = await figma.listAvailableFontsAsync()
  const has = (f, s) => list.some(x => x.fontName.family === f && x.fontName.style === s)
  for (const [f, r, b, x] of [["Atkinson Hyperlegible Next", "Regular", "Bold", "ExtraBold"], ["Atkinson Hyperlegible", "Regular", "Bold", "Bold"], ["Inter", "Regular", "Bold", "Extra Bold"]])
    if (has(f, r) && has(f, b) && has(f, x)) return { r: { family: f, style: r }, b: { family: f, style: b }, x: { family: f, style: x } }
  return { r: { family: "Inter", style: "Regular" }, b: { family: "Inter", style: "Bold" }, x: { family: "Inter", style: "Bold" } }
}
async function setupVars() {
  try {
    const cols = await figma.variables.getLocalVariableCollectionsAsync()
    let col = cols.find(c => c.name === "SignaCO")
    if (!col) col = figma.variables.createVariableCollection("SignaCO")
    const mode = col.modes[0].modeId
    const all = await figma.variables.getLocalVariablesAsync("COLOR")
    for (const [k, name] of VAR_NAMES) {
      let v = all.find(x => x.name === name && x.variableCollectionId === col.id)
      if (!v) v = figma.variables.createVariable(name, col, "COLOR")
      v.setValueForMode(mode, rgba(HEX[k], 1))
      VARS[k] = v
    }
    LOG.push("variables de color: " + VAR_NAMES.length)
  } catch (e) { LOG.push("variables no disponibles: " + e.message) }
}
async function setupStyles() {
  try {
    const ex = figma.getLocalTextStylesAsync ? await figma.getLocalTextStylesAsync() : figma.getLocalTextStyles()
    for (const k of Object.keys(STY)) {
      const d = STY[k], full = "SignaCO/" + d[0]
      let st = ex.find(x => x.name === full)
      if (!st) st = figma.createTextStyle()
      st.name = full; st.fontName = FONT[d[2]]; st.fontSize = d[1]
      st.lineHeight = { value: d[3], unit: "PERCENT" }; st.letterSpacing = { value: d[4], unit: "PERCENT" }
      TSTY[k] = st
    }
    LOG.push("estilos de texto: " + Object.keys(STY).length)
  } catch (e) { LOG.push("estilos no disponibles: " + e.message) }
}

const t0 = Date.now()
let page = figma.root.children.find(p => p.name === PAGE_NAME)
if (page) { await figma.setCurrentPageAsync(page); for (const n of page.children.slice()) n.remove() }
else { page = figma.createPage(); page.name = PAGE_NAME; await figma.setCurrentPageAsync(page) }
page.backgrounds = [{ type: "SOLID", color: rgb(HEX.tablero) }]
FONT = await pickFonts()
for (const k of ["r", "b", "x"]) await figma.loadFontAsync(FONT[k])
LOG.push("fuente: " + FONT.r.family)
await setupVars()
await setupStyles()

const g = safe("guía visual", intro)
if (g) { g.x = 0; g.y = 0 }
const Y0 = g ? Math.round(g.height) + 160 : 0
const secC = section("Componentes", 2440, Y0, 520, 1000)
safe("componentes", () => makeComponents(secC))
const s1 = section("Primer uso y aprendizaje", 0, Y0, 1400, 1080)
place(s1, [safe("permiso", sPermiso), safe("catálogo", sCatalogo), safe("lección", sLeccion)], [
  "Primer uso. El dibujo son los 21 puntos que lee MediaPipe: la misma idea que la promesa de privacidad.",
  "La columna de cuadros suma las 20 señas, una fila por lección. La fila resaltada es la lección en curso.",
  "Las miniaturas son los clips de referencia; mientras se graban, se usan diagramas provisionales."])
const s2 = section("Práctica con la cámara", 0, Y0 + 1200, 2280, 1080)
place(s2, [safe("práctica lista", () => sPractica("idle", "cámara lista", 4)), safe("práctica buscando", () => sPractica("buscando", "buscando la mano", 5)), safe("práctica reconociendo", () => sPractica("reconociendo", "reconociendo", 6)), safe("práctica acierto", () => sPractica("acierto", "acierto", 7)), safe("práctica error", () => sPractica("error", "sin coincidencia", 8))], [
  "La palabra va a 48 px; si no cabe en una línea (por ejemplo, Buenas tardes), baja a 36 px.",
  "El recuadro punteado marca el espacio donde deben estar las manos.",
  "La barra muestra la coincidencia en vivo contra el umbral de la seña.",
  "Al confirmar, vibración corta. El sello solo aparece cuando aciertas.",
  "Umbral por seña, leído de la configuración de rechazo. El mensaje dice qué pasó y cómo corregirlo."])
const s3 = section("Resultados", 0, Y0 + 2400, 960, 1080)
place(s3, [safe("quiz", sQuiz), safe("progreso", sProgreso)], [null, "Cuadros de tinta: dominadas. Amarillos: practicadas sin dominar. Una fila llena es una lección completa."])
if (PENDING.length) await Promise.all(PENDING)
figma.viewport.scrollAndZoomIntoView(page.children)
print(LOG.join("\n") + "\nTiempo: " + ((Date.now() - t0) / 1000).toFixed(1) + " s")

})()
