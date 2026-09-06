package com.diy.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 会员中心信息（由订单实付金额动态计算）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberInfoVO implements Serializable {

    private static final long serialVersionUID = 1L;

    //会员等级 0-3
    private Integer level;

    //等级名称
    private String levelName;

    //累计实付金额
    private BigDecimal totalSpent;

    //成长值（= 累计实付金额取整）
    private Integer points;

    //有效订单数
    private Integer orderCount;

    //升级到下一等级所需累计金额（已是最高级则为 null）
    private BigDecimal nextThreshold;

    //下一等级名称（已是最高级则为 null）
    private String nextLevelName;

    //当前等级进度百分比 0-100
    private Integer progressPercent;
}
