package com.diy.service;

import com.diy.entity.DiyMaterial;
import com.diy.result.PageResult;
import java.util.List;

public interface DiyMaterialService {
    
    /**
     * 查询材料列表（可选标题关键词、条数上限）
     */
    List<DiyMaterial> list(String categoryKey, String colorSeriesKey, String title, Integer limit);
    
    /**
     * 分页查询材料列表
     */
    PageResult page(Integer page, Integer pageSize, String categoryKey, String colorSeriesKey, String title);
    
    /**
     * 根据ID查询
     */
    DiyMaterial getById(Long id);
    
    /**
     * 新增材料
     */
    void add(DiyMaterial material);
    
    /**
     * 更新材料
     */
    void update(DiyMaterial material);
    
    /**
     * 删除材料
     */
    void delete(Long id);
    
    /**
     * 批量修改状态
     */
    void updateStatus(Integer status, List<Long> ids);
}
