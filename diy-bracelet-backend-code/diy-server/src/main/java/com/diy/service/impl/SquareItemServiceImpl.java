package com.diy.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.diy.dto.SquareSubmitDTO;
import com.diy.entity.SquareItem;
import com.diy.mapper.SquareItemMapper;
import com.diy.result.PageResult;
import com.diy.service.SquareItemService;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Service
@Slf4j
public class SquareItemServiceImpl implements SquareItemService {

    @Autowired
    private SquareItemMapper squareItemMapper;

    @Override
    public Long submit(SquareSubmitDTO dto, Long userId) {
        if (dto == null || !StringUtils.hasText(dto.getDiyData())) {
            throw new RuntimeException("设计数据不能为空");
        }
        String diyData = normalizeDiyData(dto.getDiyData());
        JSONObject diyJson = JSON.parseObject(diyData);

        String title = StringUtils.hasText(dto.getTitle()) ? dto.getTitle() : diyJson.getString("title");
        if (!StringUtils.hasText(title)) {
            title = "DIY设计";
        }

        String imageUrl = StringUtils.hasText(dto.getImageUrl()) ? dto.getImageUrl() : diyJson.getString("imageUrl");

        BigDecimal price = dto.getPrice();
        if (price == null && diyJson.get("price") != null) {
            price = diyJson.getBigDecimal("price");
        }
        if (price == null) {
            price = BigDecimal.ZERO;
        }

        Integer beadCount = dto.getBeadCount();
        if (beadCount == null) {
            JSONArray beads = diyJson.getJSONArray("beads");
            beadCount = beads != null ? beads.size() : 0;
        }

        BigDecimal handSize = dto.getHandSize();
        if (handSize == null && diyJson.get("size") != null) {
            handSize = diyJson.getBigDecimal("size");
        }

        SquareItem item = SquareItem.builder()
                .title(title)
                .imageUrl(imageUrl)
                .diyData(diyData)
                .price(price)
                .beadCount(beadCount)
                .handSize(handSize)
                .status(0)
                .showScope(3)
                .sort(0)
                .userId(userId)
                .cartItemId(dto.getCartItemId())
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
        squareItemMapper.insert(item);
        log.info("提交广场作品成功 id={}, userId={}", item.getId(), userId);
        return item.getId();
    }

    private String normalizeDiyData(String raw) {
        try {
            JSONObject obj = JSON.parseObject(raw);
            JSONArray beads = obj.getJSONArray("beads");
            if (beads == null || beads.isEmpty()) {
                return raw;
            }
            List<JSONObject> list = new ArrayList<>();
            for (int i = 0; i < beads.size(); i++) {
                JSONObject bead = beads.getJSONObject(i);
                if (bead != null) {
                    if (bead.getInteger("position") == null) {
                        bead.put("position", i + 1);
                    }
                    list.add(bead);
                }
            }
            list.sort(Comparator.comparingInt(o -> {
                Integer p = o.getInteger("position");
                return p != null ? p : 0;
            }));
            obj.put("beads", list);
            return obj.toJSONString();
        } catch (Exception e) {
            log.warn("规范化 diyData 失败，原样保存: {}", e.getMessage());
            return raw;
        }
    }

    @Override
    public List<SquareItem> listVisible(String channel) {
        String ch = ("mine".equalsIgnoreCase(channel) || "recommend".equalsIgnoreCase(channel))
                ? "recommend" : "square";
        List<SquareItem> list = squareItemMapper.listVisibleByChannel(ch);
        return list != null ? list : Collections.emptyList();
    }

    @Override
    public SquareItem getById(Long id) {
        return squareItemMapper.getById(id);
    }

    @Override
    public PageResult page(Integer page, Integer pageSize, Integer status, Integer showScope, String title) {
        PageHelper.startPage(page, pageSize);
        Page<SquareItem> p = (Page<SquareItem>) squareItemMapper.page(status, showScope, title);
        return new PageResult(p.getTotal(), p.getResult());
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        if (id == null || status == null || (status != 0 && status != 1)) {
            throw new RuntimeException("参数错误");
        }
        squareItemMapper.updateStatus(id, status);
    }

    @Override
    public void updateMeta(Long id, String title, Integer sort, Integer status, Integer showScope) {
        SquareItem existing = squareItemMapper.getById(id);
        if (existing == null) {
            throw new RuntimeException("作品不存在");
        }
        Integer scope = showScope;
        if (scope == null) {
            scope = existing.getShowScope() != null ? existing.getShowScope() : 3;
        }
        if (scope != 1 && scope != 2 && scope != 3) {
            throw new RuntimeException("展示范围参数错误");
        }
        SquareItem item = SquareItem.builder()
                .id(id)
                .title(StringUtils.hasText(title) ? title : existing.getTitle())
                .sort(sort != null ? sort : (existing.getSort() != null ? existing.getSort() : 0))
                .status(status != null ? status : existing.getStatus())
                .showScope(scope)
                .build();
        squareItemMapper.updateMeta(item);
    }

    @Override
    public void delete(Long id) {
        squareItemMapper.deleteById(id);
    }
}
