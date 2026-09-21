package com.diy.controller.user;

import com.diy.context.BaseContext;
import com.diy.dto.SquareSubmitDTO;
import com.diy.entity.SquareItem;
import com.diy.result.Result;
import com.diy.service.SquareItemService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController("userSquareController")
@RequestMapping("/user/square")
@Api(tags = "用户端-灵感广场")
@Slf4j
public class SquareController {

    @Autowired
    private SquareItemService squareItemService;

    @PostMapping("/submit")
    @ApiOperation("购物车设计提交（待后台审核）")
    public Result<Map<String, Object>> submit(@RequestBody SquareSubmitDTO dto) {
        Long userId = BaseContext.getCurrentId();
        Long id = squareItemService.submit(dto, userId);
        Map<String, Object> data = new HashMap<>();
        data.put("id", id);
        return Result.success(data);
    }

    @GetMapping("/list")
    @ApiOperation("已展示作品列表 channel=square|recommend|mine")
    public Result<List<Map<String, Object>>> list(
            @RequestParam(defaultValue = "square") String channel) {
        List<Map<String, Object>> list = squareItemService.listVisible(channel).stream()
                .map(this::toCard)
                .collect(Collectors.toList());
        return Result.success(list);
    }

    @GetMapping("/detail/{id}")
    @ApiOperation("作品详情")
    public Result<Map<String, Object>> detail(
            @PathVariable Long id,
            @RequestParam(required = false) String channel) {
        SquareItem item = squareItemService.getById(id);
        if (item == null) {
            return Result.error("作品不存在");
        }
        if (item.getStatus() == null || item.getStatus() != 1) {
            return Result.error("作品未上架");
        }
        Integer scope = item.getShowScope() != null ? item.getShowScope() : 3;
        if ("mine".equalsIgnoreCase(channel) || "recommend".equalsIgnoreCase(channel)) {
            if (scope != 2 && scope != 3) {
                return Result.error("作品未上架");
            }
        } else if ("square".equalsIgnoreCase(channel)) {
            if (scope != 1 && scope != 3) {
                return Result.error("作品未上架");
            }
        }
        return Result.success(toDetail(item));
    }

    private Map<String, Object> toCard(SquareItem item) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", item.getId());
        m.put("title", item.getTitle());
        m.put("imageUrl", item.getImageUrl());
        m.put("price", item.getPrice());
        m.put("beadCount", item.getBeadCount());
        m.put("handSize", item.getHandSize());
        m.put("showScope", item.getShowScope());
        m.put("createTime", item.getCreateTime());
        return m;
    }

    private Map<String, Object> toDetail(SquareItem item) {
        Map<String, Object> m = toCard(item);
        m.put("diyData", item.getDiyData());
        return m;
    }
}
