package com.diy.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SquareItem implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String title;
    private String imageUrl;
    /** DIY设计JSON，含 beads 顺序 */
    private String diyData;
    private BigDecimal price;
    private Integer beadCount;
    private BigDecimal handSize;
    /** 0隐藏 1展示 */
    private Integer status;
    /** 1仅灵感广场 2仅推荐设计 3灵感广场+推荐设计 */
    private Integer showScope;
    private Integer sort;
    private Long userId;
    private Long cartItemId;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
