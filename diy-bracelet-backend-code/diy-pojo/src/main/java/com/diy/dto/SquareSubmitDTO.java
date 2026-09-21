package com.diy.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@ApiModel("提交广场作品")
public class SquareSubmitDTO implements Serializable {

    @ApiModelProperty("标题")
    private String title;

    @ApiModelProperty("设计图URL")
    private String imageUrl;

    @ApiModelProperty("DIY设计JSON字符串（含珠子顺序）")
    private String diyData;

    @ApiModelProperty("金额")
    private BigDecimal price;

    @ApiModelProperty("珠子数量")
    private Integer beadCount;

    @ApiModelProperty("手围cm")
    private BigDecimal handSize;

    @ApiModelProperty("来源购物车项ID")
    private Long cartItemId;
}
