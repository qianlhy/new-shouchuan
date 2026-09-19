/**
 * 导出底部 tabBar 图标：细线 + 真 81 / @3x(243)
 * 未选中灰、选中主紫，对齐 theme $primary
 */
const lucide = require('lucide-static')
const sharp = require('sharp')
const path = require('path')
const fs = require('fs')

const OUT = path.join(__dirname, '..', 'static', 'tabbar')
const STROKE = 1.75
const GRAY = '#9CA3AF'
const PURPLE = '#8B5CF6'

const MAP = {
  home: 'House',
  square: 'Send',
  design: 'Orbit',
  cart: 'ShoppingCart',
  mine: 'User'
}

function prepSvg(raw, color, size) {
  let s = String(raw)
  s = s.replace(/stroke="currentColor"/g, `stroke="${color}"`)
  s = s.replace(/stroke-width="2"/g, `stroke-width="${STROKE}"`)
  if (!/xmlns=/.test(s)) {
    s = s.replace('<svg', '<svg xmlns="http://www.w3.org/2000/svg"')
  }
  s = s.replace(/width="24"/, `width="${size}"`)
  s = s.replace(/height="24"/, `height="${size}"`)
  return s
}

async function writeSized(name, svg, px) {
  const buf = Buffer.from(svg)
  // 留白边，避免描边贴边发糊
  const pad = Math.round(px * 0.14)
  const inner = px - pad * 2
  const icon = await sharp(buf, { density: 400 }).resize(inner, inner).png().toBuffer()
  const out = await sharp({
    create: {
      width: px,
      height: px,
      channels: 4,
      background: { r: 0, g: 0, b: 0, alpha: 0 }
    }
  })
    .composite([{ input: icon, left: pad, top: pad }])
    .png({ compressionLevel: 9 })
    .toBuffer()
  fs.writeFileSync(path.join(OUT, name), out)
}

;(async () => {
  if (!fs.existsSync(OUT)) fs.mkdirSync(OUT, { recursive: true })

  for (const [file, key] of Object.entries(MAP)) {
    const raw = lucide[key]
    if (!raw) {
      console.log('MISS', key)
      continue
    }

    const sizes = [
      { suffix: '', px: 81 },
      { suffix: '@3x', px: 243 }
    ]

    for (const { suffix, px } of sizes) {
      await writeSized(`${file}${suffix}.png`, prepSvg(raw, GRAY, px), px)
      await writeSized(`${file}_selected${suffix}.png`, prepSvg(raw, PURPLE, px), px)
    }
    console.log('ok', file)
  }
  console.log('TABBAR DONE')
})().catch((e) => {
  console.error(e)
  process.exit(1)
})
