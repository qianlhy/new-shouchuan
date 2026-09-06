package com.diy.service.impl;

import com.diy.context.BaseContext;
import com.diy.mapper.ProductWishMapper;
import com.diy.service.ProductWishService;
import com.diy.vo.WishStatusVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class ProductWishServiceImpl implements ProductWishService {

    @Autowired
    private ProductWishMapper productWishMapper;

    @Override
    public WishStatusVO toggle(Long productId) {
        Long userId = BaseContext.getCurrentId();
        boolean existed = productWishMapper.exists(userId, productId) > 0;
        if (existed) {
            productWishMapper.delete(userId, productId);
        } else {
            productWishMapper.insert(userId, productId);
        }
        int count = productWishMapper.countByProduct(productId);
        return WishStatusVO.builder()
                .productId(productId)
                .wantCount(count)
                .goalCount(GOAL)
                .achieved(count >= GOAL)
                .wanted(!existed)
                .build();
    }

    @Override
    public List<WishStatusVO> counts(List<Long> ids) {
        List<WishStatusVO> result = new ArrayList<>();
        if (ids == null || ids.isEmpty()) {
            return result;
        }
        Map<Long, Integer> countMap = new HashMap<>();
        List<Map<String, Object>> rows = productWishMapper.countByProducts(ids);
        for (Map<String, Object> row : rows) {
            Long pid = Long.valueOf(String.valueOf(row.get("productId")));
            Integer cnt = Integer.valueOf(String.valueOf(row.get("wantCount")));
            countMap.put(pid, cnt);
        }
        for (Long id : ids) {
            int count = countMap.getOrDefault(id, 0);
            result.add(WishStatusVO.builder()
                    .productId(id)
                    .wantCount(count)
                    .goalCount(GOAL)
                    .achieved(count >= GOAL)
                    .wanted(false)
                    .build());
        }
        return result;
    }

    @Override
    public List<Map<String, Object>> myWishes() {
        Long userId = BaseContext.getCurrentId();
        List<Map<String, Object>> list = productWishMapper.listWishedProductsByUser(userId);
        for (Map<String, Object> row : list) {
            int count = row.get("wantCount") == null ? 0 : Integer.parseInt(String.valueOf(row.get("wantCount")));
            row.put("goalCount", GOAL);
            row.put("achieved", count >= GOAL);
        }
        return list;
    }

    @Override
    public List<Long> myWishProductIds() {
        Long userId = BaseContext.getCurrentId();
        return productWishMapper.listProductIdsByUser(userId);
    }
}
