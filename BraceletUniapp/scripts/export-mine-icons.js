const lucide = require('lucide-static')
const sharp = require('sharp')
const path = require('path')

const OUT = path.join(__dirname, '..', 'static', 'icons')
const SIZE = 256
const COLOR = '#6B4EFF'
const STROKE = 2.35

const MAP = {
  wallet: 'Wallet',
  package: 'Package',
  truck: 'Truck',
  'circle-check': 'CircleCheck',
  palette: 'Palette',
  sparkles: 'Sparkles',
  heart: 'Heart',
  'layout-grid': 'LayoutGrid',
  'map-pin': 'MapPin',
  headphones: 'Headphones',
  settings: 'Settings',
  info: 'Info',
  crown: 'Crown',
  gift: 'Gift',
  gem: 'Gem',
  'shield-check': 'ShieldCheck',
  'circle-help': 'CircleHelp',
  'message-circle': 'MessageCircle',
  'refresh-cw': 'RefreshCw',
  user: 'User',
  star: 'Star',
  eye: 'Eye',
  pencil: 'Pencil',
  'shopping-bag': 'ShoppingBag',
  'shopping-cart': 'ShoppingCart',
  'badge-percent': 'BadgePercent'
}

function prepSvg(raw) {
  let s = String(raw)
  s = s.replace(/stroke="currentColor"/g, `stroke="${COLOR}"`)
  s = s.replace(/stroke-width="2"/g, `stroke-width="${STROKE}"`)
  if (!/xmlns=/.test(s)) {
    s = s.replace('<svg', '<svg xmlns="http://www.w3.org/2000/svg"')
  }
  s = s.replace(/width="24"/, `width="${SIZE}"`)
  s = s.replace(/height="24"/, `height="${SIZE}"`)
  return s
}

;(async () => {
  for (const [file, key] of Object.entries(MAP)) {
    const raw = lucide[key]
    if (!raw) {
      console.log('MISS', key)
      continue
    }
    const svg = prepSvg(raw)
    const buf = Buffer.from(svg)
    const png = await sharp(buf, { density: 400 }).resize(SIZE, SIZE).png({ compressionLevel: 9 }).toBuffer()
    require('fs').writeFileSync(path.join(OUT, `${file}.png`), png)
    require('fs').writeFileSync(path.join(OUT, `${file}-purple.png`), png)
    console.log('ok', file)
  }
  console.log('ALL DONE')
})().catch((e) => {
  console.error(e)
  process.exit(1)
})
