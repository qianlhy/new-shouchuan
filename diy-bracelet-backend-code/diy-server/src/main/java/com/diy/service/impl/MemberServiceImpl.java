package com.diy.service.impl;

import com.diy.context.BaseContext;
import com.diy.mapper.OrderMapper;
import com.diy.service.MemberService;
import com.diy.vo.MemberInfoVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@Slf4j
public class MemberServiceImpl implements MemberService {

    @Autowired
    private OrderMapper orderMapper;

    //各等级累计实付金额门槛
    private static final BigDecimal[] THRESHOLDS = {
            BigDecimal.ZERO,
            new BigDecimal("500"),
            new BigDecimal("2000"),
            new BigDecimal("5000")
    };
    private static final String[] LEVEL_NAMES = {"普通会员", "银卡会员", "金卡会员", "黑钻会员"};

    @Override
    public MemberInfoVO getCurrentMemberInfo() {
        Long userId = BaseContext.getCurrentId();

        BigDecimal totalSpent = orderMapper.sumPaidAmountByUser(userId);
        if (totalSpent == null) {
            totalSpent = BigDecimal.ZERO;
        }
        Integer orderCount = orderMapper.countPaidByUser(userId);
        if (orderCount == null) {
            orderCount = 0;
        }

        // 计算等级
        int level = 0;
        for (int i = THRESHOLDS.length - 1; i >= 0; i--) {
            if (totalSpent.compareTo(THRESHOLDS[i]) >= 0) {
                level = i;
                break;
            }
        }

        // 下一等级
        BigDecimal nextThreshold = null;
        String nextLevelName = null;
        int progressPercent = 100;
        if (level < THRESHOLDS.length - 1) {
            BigDecimal cur = THRESHOLDS[level];
            BigDecimal next = THRESHOLDS[level + 1];
            nextThreshold = next;
            nextLevelName = LEVEL_NAMES[level + 1];
            BigDecimal span = next.subtract(cur);
            BigDecimal done = totalSpent.subtract(cur);
            if (span.compareTo(BigDecimal.ZERO) > 0) {
                progressPercent = done.multiply(new BigDecimal("100"))
                        .divide(span, 0, RoundingMode.FLOOR)
                        .intValue();
                if (progressPercent > 99) {
                    progressPercent = 99;
                }
                if (progressPercent < 0) {
                    progressPercent = 0;
                }
            }
        }

        return MemberInfoVO.builder()
                .level(level)
                .levelName(LEVEL_NAMES[level])
                .totalSpent(totalSpent)
                .points(totalSpent.setScale(0, RoundingMode.FLOOR).intValue())
                .orderCount(orderCount)
                .nextThreshold(nextThreshold)
                .nextLevelName(nextLevelName)
                .progressPercent(progressPercent)
                .build();
    }
}
