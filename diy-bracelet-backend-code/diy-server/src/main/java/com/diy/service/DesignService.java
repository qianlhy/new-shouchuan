package com.diy.service;

import com.diy.dto.DiyOrderCreateDTO;
import com.diy.entity.ColorSeries;
import com.diy.entity.DiyCategory;
import com.diy.entity.DiyMaterial;
import com.diy.entity.Orders;
import com.diy.result.PageResult;
import java.util.List;

public interface DesignService {

    /**
     * 查询所有DIY分类
     * @return
     */
    List<DiyCategory> getCategoryList();

    /**
     * 查询所有色系
     * @return
     */
    List<ColorSeries> getColorSeriesList();

    /**
     * 查询DIY材料列表（支持分类和色系筛选,支持分页）
     * @param categories 分类键列表
     * @param colorSeries 色系键列表
     * @param page 页码（从1开始）
     * @param pageSize 每页数量
     * @return
     */
    PageResult getMaterialList(List<String> categories, List<String> colorSeries, Integer page, Integer pageSize);

    /**
     * 创建DIY订单
     * @param diyOrderCreateDTO 订单数据
     * @return
     */
    Orders createDiyOrder(DiyOrderCreateDTO diyOrderCreateDTO);
}
