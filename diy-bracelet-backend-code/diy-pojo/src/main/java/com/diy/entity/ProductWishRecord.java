package com.diy.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 商品心愿众筹记录
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductWishRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    //用户ID
    private Long userId;

    //商品ID
    private Long productId;

    //想要时间
    private LocalDateTime createTime;
}
