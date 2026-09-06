package com.diy.mapper;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface ProductWishMapper {

    /**
     * 当前用户是否已想要该商品
     */
    @Select("select count(*) from product_wish_record where user_id=#{userId} and product_id=#{productId}")
    int exists(@Param("userId") Long userId, @Param("productId") Long productId);

    /**
     * 新增想要记录
     */
    @Insert("insert into product_wish_record(user_id, product_id, create_time) values(#{userId},#{productId},now())")
    void insert(@Param("userId") Long userId, @Param("productId") Long productId);

    /**
     * 取消想要
     */
    @Delete("delete from product_wish_record where user_id=#{userId} and product_id=#{productId}")
    void delete(@Param("userId") Long userId, @Param("productId") Long productId);

    /**
     * 单个商品想要人数
     */
    @Select("select count(*) from product_wish_record where product_id=#{productId}")
    int countByProduct(Long productId);

    /**
     * 批量商品想要人数
     */
    List<Map<String, Object>> countByProducts(@Param("ids") List<Long> ids);

    /**
     * 当前用户想要的商品ID
     */
    @Select("select product_id from product_wish_record where user_id=#{userId}")
    List<Long> listProductIdsByUser(Long userId);

    /**
     * 当前用户想要的商品（含商品信息，用于「我的收藏」）
     */
    List<Map<String, Object>> listWishedProductsByUser(Long userId);
}
