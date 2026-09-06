package com.diy.mapper;

import com.diy.entity.DiyMaterial;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface DiyMaterialMapper {

    /**
     * 查询DIY材料列表（支持分类和色系筛选）
     * 
     * @param categories  分类键列表
     * @param colorSeries 色系键列表
     * @return
     */
    List<DiyMaterial> list(@Param("categories") List<String> categories,
            @Param("colorSeries") List<String> colorSeries);

    /**
     * 用户端材料列表（按名称+尺寸排序,支持分页）
     */
    List<DiyMaterial> listForUser(@Param("categories") List<String> categories,
            @Param("colorSeries") List<String> colorSeries,
            @Param("offset") Integer offset,
            @Param("pageSize") Integer pageSize);

    /**
     * 统计符合条件的材料数量（用于分页）
     */
    int countForUser(@Param("categories") List<String> categories,
            @Param("colorSeries") List<String> colorSeries);

    /**
     * 根据ID查询材料
     */
    DiyMaterial getById(Long id);

    /**
     * 新增材料
     */
    void insert(DiyMaterial material);

    /**
     * 更新材料
     */
    void update(DiyMaterial material);

    /**
     * 删除材料
     */
    void delete(Long id);

    /**
     * 更新状态
     */
    void updateStatus(@Param("id") Long id, @Param("status") Integer status);

    /**
     * 扣减库存（原子操作）
     * 
     * @param materialId 材料ID
     * @param quantity   扣减数量
     * @return 影响行数（0表示库存不足）
     */
    int deductStock(@Param("materialId") Long materialId, @Param("quantity") Integer quantity);

    /**
     * 回滚库存（原子操作）
     * 
     * @param materialId 材料ID
     * @param quantity   回滚数量
     */
    void rollbackStock(@Param("materialId") Long materialId, @Param("quantity") Integer quantity);
}
