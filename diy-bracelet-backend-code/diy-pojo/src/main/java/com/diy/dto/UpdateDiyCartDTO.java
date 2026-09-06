package com.diy.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
@ApiModel(description = "更新DIY购物车设计DTO")
public class UpdateDiyCartDTO implements Serializable {

    @ApiModelProperty("购物车项ID")
    private Long id;

    @ApiModelProperty("DIY设计数据（JSON）")
    private String diyData;
}
