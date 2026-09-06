package com.diy.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * 订单地址修改DTO
 */
@Data
@ApiModel(description = "订单地址修改信息")
public class OrderAddressUpdateDTO implements Serializable {

    @NotBlank(message = "收货人姓名不能为空")
    @ApiModelProperty(value = "收货人姓名", required = true)
    private String name;

    @NotBlank(message = "手机号不能为空")
    @ApiModelProperty(value = "手机号", required = true)
    private String phone;

    @NotBlank(message = "省份不能为空")
    @ApiModelProperty(value = "省份", required = true)
    private String province;

    @NotBlank(message = "城市不能为空")
    @ApiModelProperty(value = "城市", required = true)
    private String city;

    @NotBlank(message = "区/县不能为空")
    @ApiModelProperty(value = "区/县", required = true)
    private String district;

    @NotBlank(message = "详细地址不能为空")
    @ApiModelProperty(value = "详细地址", required = true)
    private String detail;

    private static final long serialVersionUID = 1L;
}
