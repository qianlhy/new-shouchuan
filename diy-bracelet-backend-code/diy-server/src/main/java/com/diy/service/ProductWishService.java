package com.diy.service;

import com.diy.vo.WishStatusVO;

import java.util.List;
import java.util.Map;

public interface ProductWishService {

    /**
     * 达成心愿的目标人数
     */
    int GOAL = 100;

    /**
     * 切换「想要」状态（已想要则取消，否则想要）
     */
    WishStatusVO toggle(Long productId);

    /**
     * 批量查询商品的众筹进度（公开，无需登录）
     */
    List<WishStatusVO> counts(List<Long> ids);

    /**
     * 当前用户想要的商品（含商品信息）
     */
    List<Map<String, Object>> myWishes();

    /**
     * 当前用户想要的商品ID集合
     */
    List<Long> myWishProductIds();
}
