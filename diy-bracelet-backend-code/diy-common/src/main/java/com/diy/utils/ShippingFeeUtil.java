package com.diy.utils;

import java.math.BigDecimal;

/**
 * 运费计算（下单、展示共用同一规则，避免前后端不一致）
 * <ul>
 *   <li>新疆 / 西藏：固定加 15 元偏远地区运费（不再叠加不满20的7元）</li>
 *   <li>其他地区：商品金额不满 20 元加 7 元运费</li>
 * </ul>
 */
public final class ShippingFeeUtil {

    public static final BigDecimal FREE_SHIPPING_THRESHOLD = new BigDecimal("20");
    public static final BigDecimal LOW_AMOUNT_FEE = new BigDecimal("7");
    public static final BigDecimal REMOTE_AREA_FEE = new BigDecimal("15");

    private ShippingFeeUtil() {
    }

    /**
     * @param goodsAmount 商品金额（不含运费）
     * @param province    收货省份，可为 null
     */
    public static BigDecimal calculate(BigDecimal goodsAmount, String province) {
        // 偏远地区只收固定偏远运费，不叠加「不满20加7」
        if (isRemoteArea(province)) {
            return REMOTE_AREA_FEE;
        }
        BigDecimal goods = goodsAmount == null ? BigDecimal.ZERO : goodsAmount;
        if (goods.compareTo(FREE_SHIPPING_THRESHOLD) < 0) {
            return LOW_AMOUNT_FEE;
        }
        return BigDecimal.ZERO;
    }

    public static boolean isRemoteArea(String province) {
        if (province == null || province.isEmpty()) {
            return false;
        }
        return province.contains("新疆") || province.contains("西藏");
    }
}
