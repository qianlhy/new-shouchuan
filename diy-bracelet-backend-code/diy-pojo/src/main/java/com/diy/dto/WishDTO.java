package com.diy.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 心愿众筹「想要」DTO
 */
@Data
@ApiModel(description = "心愿众筹想要DTO")
public class WishDTO implements Serializable {

    @ApiModelProperty("商品ID")
    private Long productId;
}
