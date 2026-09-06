package com.diy.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 心愿众筹状态
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WishStatusVO implements Serializable {

    private static final long serialVersionUID = 1L;

    //商品ID
    private Long productId;

    //已想要人数
    private Integer wantCount;

    //达成目标人数
    private Integer goalCount;

    //是否已达成
    private Boolean achieved;

    //当前用户是否已想要
    private Boolean wanted;
}
