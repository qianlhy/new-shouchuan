package com.diy.mapper;

import com.diy.entity.SquareItem;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface SquareItemMapper {

    @Insert("INSERT INTO square_item(title, image_url, diy_data, price, bead_count, hand_size, status, show_scope, sort, user_id, cart_item_id, create_time, update_time) " +
            "VALUES(#{title}, #{imageUrl}, #{diyData}, #{price}, #{beadCount}, #{handSize}, #{status}, #{showScope}, #{sort}, #{userId}, #{cartItemId}, #{createTime}, #{updateTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(SquareItem item);

    @Select("SELECT id, title, image_url AS imageUrl, diy_data AS diyData, price, bead_count AS beadCount, " +
            "hand_size AS handSize, status, show_scope AS showScope, sort, user_id AS userId, cart_item_id AS cartItemId, " +
            "create_time AS createTime, update_time AS updateTime FROM square_item WHERE id = #{id}")
    SquareItem getById(Long id);

    /**
     * channel: square=灵感广场(show_scope 1或3), recommend|mine=推荐设计(show_scope 2或3)
     */
    @Select("<script>" +
            "SELECT id, title, image_url AS imageUrl, diy_data AS diyData, price, bead_count AS beadCount, " +
            "hand_size AS handSize, status, show_scope AS showScope, sort, user_id AS userId, cart_item_id AS cartItemId, " +
            "create_time AS createTime, update_time AS updateTime FROM square_item " +
            "WHERE status = 1 " +
            "<choose>" +
            "<when test='channel == \"square\"'> AND show_scope IN (1, 3) </when>" +
            "<when test='channel == \"mine\" or channel == \"recommend\"'> AND show_scope IN (2, 3) </when>" +
            "<otherwise> AND show_scope IN (1, 2, 3) </otherwise>" +
            "</choose>" +
            "ORDER BY sort DESC, id DESC" +
            "</script>")
    List<SquareItem> listVisibleByChannel(@Param("channel") String channel);

    @Select("<script>" +
            "SELECT id, title, image_url AS imageUrl, diy_data AS diyData, price, bead_count AS beadCount, " +
            "hand_size AS handSize, status, show_scope AS showScope, sort, user_id AS userId, cart_item_id AS cartItemId, " +
            "create_time AS createTime, update_time AS updateTime FROM square_item " +
            "<where>" +
            "<if test='status != null'> AND status = #{status} </if>" +
            "<if test='showScope != null'> AND show_scope = #{showScope} </if>" +
            "<if test='title != null and title != \"\"'> AND title LIKE CONCAT('%', #{title}, '%') </if>" +
            "</where>" +
            "ORDER BY sort DESC, id DESC" +
            "</script>")
    List<SquareItem> page(@Param("status") Integer status, @Param("showScope") Integer showScope, @Param("title") String title);

    @Update("UPDATE square_item SET status = #{status}, update_time = NOW() WHERE id = #{id}")
    void updateStatus(@Param("id") Long id, @Param("status") Integer status);

    @Update("UPDATE square_item SET title = #{title}, sort = #{sort}, status = #{status}, show_scope = #{showScope}, update_time = NOW() WHERE id = #{id}")
    void updateMeta(SquareItem item);

    @Delete("DELETE FROM square_item WHERE id = #{id}")
    void deleteById(Long id);
}
