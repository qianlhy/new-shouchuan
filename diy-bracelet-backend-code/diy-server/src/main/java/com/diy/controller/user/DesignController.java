package com.diy.controller.user;

import com.diy.dto.DiyOrderCreateDTO;
import com.diy.entity.ColorSeries;
import com.diy.entity.DiyCategory;
import com.diy.entity.DiyMaterial;
import com.diy.entity.Orders;
import com.diy.result.PageResult;
import com.diy.result.Result;
import com.diy.service.DesignService;
import com.diy.vo.DiyCategoryWithChildrenVO;
import com.diy.vo.DiyChildCategoryVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.LinkedHashMap;

@RestController("userDesignController")
@RequestMapping("/user/design")
@Api(tags = "用户端DIY设计接口")
@Slf4j
public class DesignController {

    @Autowired
    private DesignService designService;

    /**
     * 查询DIY分类列表
     *
     * @return
     */
    @GetMapping("/category/list")
    @ApiOperation("查询DIY分类列表")
    public Result<List<DiyCategory>> getCategoryList() {
        log.info("查询DIY分类列表");
        List<DiyCategory> categories = designService.getCategoryList();
        return Result.success(categories);
    }

    /**
     * 查询父子分类列表（DIY分类 + 子分类）
     *
     * @return
     */
    @GetMapping("/colorSeries/list")
    @ApiOperation("查询父子分类列表（diy_category + color_series）")
    public Result<List<DiyCategoryWithChildrenVO>> getColorSeriesList() {
        log.info("查询父子分类列表（DIY分类+子分类）");

        List<DiyCategory> parents = designService.getCategoryList();
        List<ColorSeries> children = designService.getColorSeriesList();

        // 按子分类组合键前缀分组：<parent>__<child>
        Map<String, List<DiyChildCategoryVO>> childrenByParent = new LinkedHashMap<>();
        for (ColorSeries cs : children) {
            if (cs.getKeyCode() == null) {
                continue;
            }
            String key = cs.getKeyCode();
            int sep = key.indexOf("__");
            if (sep <= 0) {
                // 忽略非组合键（历史色系键）,避免污染子分类
                continue;
            }
            String parentKey = key.substring(0, sep);
            childrenByParent
                    .computeIfAbsent(parentKey, k -> new ArrayList<>())
                    .add(DiyChildCategoryVO.builder().keyCode(cs.getKeyCode()).name(cs.getName()).build());
        }

        List<DiyCategoryWithChildrenVO> result = new ArrayList<>();
        for (DiyCategory parent : parents) {
            result.add(DiyCategoryWithChildrenVO.builder()
                    .keyCode(parent.getKeyCode())
                    .name(parent.getName())
                    .children(childrenByParent.getOrDefault(parent.getKeyCode(), new ArrayList<>()))
                    .build());
        }

        return Result.success(result);
    }

    /**
     * 查询DIY材料列表（支持分类和色系筛选,支持分页）
     *
     * @param categories  分类键,多个用逗号分隔
     * @param colorSeries 色系键,多个用逗号分隔
     * @param page        页码,默认1
     * @param size        每页数量,默认20
     * @return
     */
    @GetMapping("/material/list")
    @ApiOperation("查询DIY材料列表（含二级分类映射,支持分页）")
    public Result<Map<String, Object>> getMaterialList(
            @RequestParam(required = false) String categories,
            @RequestParam(required = false) String colorSeries,
            @ApiParam(value = "页码", defaultValue = "1") @RequestParam(defaultValue = "1") Integer page,
            @ApiParam(value = "每页数量", defaultValue = "20") @RequestParam(defaultValue = "20") Integer size) {

        log.info("收到材料列表请求 - 原始参数 categories: [{}], colorSeries: [{}], page: {}, size: {}", categories, colorSeries, page, size);

        // 解析筛选参数
        List<String> categoryList = null;
        if (categories != null && !categories.trim().isEmpty()) {
            categoryList = Arrays.asList(categories.split(","));
        }

        List<String> colorSeriesList = null;
        if (colorSeries != null && !colorSeries.trim().isEmpty()) {
            colorSeriesList = Arrays.asList(colorSeries.split(","));
        }

        // 预取所有启用的色系,供后续中文名/键值混合映射与子分类列表使用
        List<ColorSeries> allSeries = designService.getColorSeriesList();
        Map<String, ColorSeries> seriesMap = new HashMap<>();
        for (ColorSeries cs : allSeries) {
            seriesMap.put(cs.getKeyCode(), cs);
        }

        // 将 colorSeries 入参（可能是中文名、旧键或组合键）映射为用于查询的组合键集合
        List<String> colorSeriesForQuery = colorSeriesList;
        if (colorSeriesList != null && !colorSeriesList.isEmpty()) {
            java.util.Set<String> resolved = new java.util.LinkedHashSet<>();
            for (String csCond : colorSeriesList) {
                if (csCond == null || csCond.trim().isEmpty()) {
                    continue;
                }

                String cs = csCond.trim();

                // 1) 已是组合键：直接使用
                if (cs.contains("__")) {
                    resolved.add(cs);
                    continue;
                }
                // 2) 可能是已有 key_code（兼容旧客户端直接传键值）
                if (seriesMap.containsKey(cs)) {
                    resolved.add(cs);
                    continue;
                }
                // 3) 视为中文名称,在指定父类下（如有）或全量中按名称匹配
                if (categoryList != null && !categoryList.isEmpty()) {
                    for (String cat : categoryList) {
                        for (ColorSeries s : allSeries) {
                            String key = s.getKeyCode();
                            if (key.startsWith(cat + "__") && cs.equals(s.getName())) {
                                resolved.add(key);
                            }
                        }
                    }
                } else {
                    for (ColorSeries s : allSeries) {
                        if (cs.equals(s.getName())) {
                            resolved.add(s.getKeyCode());
                        }
                    }
                }
            }
            colorSeriesForQuery = new java.util.ArrayList<>(resolved);
        }

        // 查询材料列表（已按名称+尺寸排序,支持分页）
        PageResult pageResult = designService.getMaterialList(categoryList, colorSeriesForQuery, page, size);

        // 构建响应：返回 materials、total、page、size
        Map<String, Object> resp = new HashMap<>();
        resp.put("materials", pageResult.getRecords());
        resp.put("total", pageResult.getTotal());
        resp.put("page", page);
        resp.put("size", size);
        resp.put("totalPages", (pageResult.getTotal() + size - 1) / size);

        log.info("材料数: {}, 总数: {}", pageResult.getRecords().size(), pageResult.getTotal());
        return Result.success(resp);
    }

    /**
     * 创建DIY订单
     *
     * @param diyOrderCreateDTO 订单数据
     * @return
     */
    @PostMapping("/order/create")
    @ApiOperation("创建DIY订单")
    public Result<Map<String, Object>> createDiyOrder(@RequestBody DiyOrderCreateDTO diyOrderCreateDTO) {
        log.info("创建DIY订单,订单项数量: {}", diyOrderCreateDTO.getItems() != null ? diyOrderCreateDTO.getItems().size() : 0);

        Orders order = designService.createDiyOrder(diyOrderCreateDTO);

        // 构建返回数据
        Map<String, Object> result = new HashMap<>();
        result.put("id", order.getId());
        result.put("orderNo", order.getOrderNo());
        result.put("amount", order.getAmount());
        result.put("status", order.getStatus());

        return Result.success(result);
    }
}
