package com.diy.enumeration;

import lombok.Getter;

import java.util.Arrays;
import java.util.Optional;

/**
 * 快递状态枚举
 */
@Getter
public enum KuaidiStateEnum {

    /**
     * 0：在途，即货物处于运输过程中
     */
    IN_TRANSIT(0, "在途"),

    /**
     * 1：揽件，即货物已被快递公司揽收
     */
    PICKED_UP(1, "揽件"),

    /**
     * 2：疑难，即货物在运输过程中遇到问题
     */
    DIFFICULT(2, "疑难"),

    /**
     * 3：签收，即货物已被签收
     */
    SIGNED(3, "签收"),

    /**
     * 4：退签，即货物被退回发件人
     */
    RETURNED(4, "退签"),

    /**
     * 5：派件，即货物正在派送中
     */
    DELIVERING(5, "派件"),

    /**
     * 6：退回，即货物被退回
     */
    RETURNING(6, "退回"),

    /**
     * 7：转投，即货物转投其他地址
     */
    TRANSFERRED(7, "转投"),

    /**
     * 10：待清关
     */
    WAITING_CUSTOMS(10, "待清关"),

    /**
     * 11：清关中
     */
    IN_CUSTOMS(11, "清关中"),

    /**
     * 12：已清关
     */
    CLEARED_CUSTOMS(12, "已清关"),

    /**
     * 13：清关异常
     */
    CUSTOMS_EXCEPTION(13, "清关异常"),

    /**
     * 14：拒签
     */
    REJECTED(14, "拒签");

    private final Integer code;
    private final String desc;

    KuaidiStateEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 根据状态码获取枚举
     *
     * @param code 状态码
     * @return 枚举对象
     */
    public static KuaidiStateEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        Optional<KuaidiStateEnum> optional = Arrays.stream(values())
                .filter(item -> item.getCode().equals(code))
                .findFirst();
        return optional.orElse(null);
    }

    /**
     * 根据状态码获取描述
     *
     * @param code 状态码
     * @return 描述
     */
    public static String getDescByCode(Integer code) {
        KuaidiStateEnum stateEnum = getByCode(code);
        return stateEnum != null ? stateEnum.getDesc() : "未知状态";
    }
}
