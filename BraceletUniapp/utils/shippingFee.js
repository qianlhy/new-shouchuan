/**
 * 运费计算（与后端 ShippingFeeUtil 保持一致）
 * - 新疆 / 西藏：固定 15 元偏远运费（不叠加不满20的7元）
 * - 其他地区：商品金额不满 20 元加 7 元运费
 */

export const FREE_SHIPPING_THRESHOLD = 20
export const LOW_AMOUNT_FEE = 7
export const REMOTE_AREA_FEE = 15

export function isRemoteArea(province) {
  if (!province) return false
  const p = String(province)
  return p.includes('新疆') || p.includes('西藏')
}

/**
 * @param {number|string} goodsAmount 商品金额（不含运费）
 * @param {string} [province] 收货省份
 * @returns {number} 运费
 */
export function calculateShippingFee(goodsAmount, province) {
  // 偏远地区只收固定偏远运费，不叠加「不满20加7」
  if (isRemoteArea(province)) {
    return REMOTE_AREA_FEE
  }
  const goods = Number(goodsAmount) || 0
  if (goods < FREE_SHIPPING_THRESHOLD) {
    return LOW_AMOUNT_FEE
  }
  return 0
}
