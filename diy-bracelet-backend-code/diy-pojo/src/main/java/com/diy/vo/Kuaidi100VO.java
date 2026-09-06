package com.diy.vo;

import lombok.Data;
import java.util.List;

/**
 * 快递100查询结果VO
 */
@Data
public class Kuaidi100VO {

    /**
     * 消息
     */
    private String message;

    /**
     * 状态码
     */
    private String state;

    /**
     * 状态描述
     */
    private String stateDesc;

    /**
     * 是否签收
     */
    private Boolean isSign;

    /**
     * 快递公司编码
     */
    private String com;

    /**
     * 快递单号
     */
    private String num;

    /**
     * 物流轨迹数据
     */
    private List<KuaidiStepVO> data;

    /**
     * 物流轨迹步骤VO
     */
    @Data
    public static class KuaidiStepVO {
        /**
         * 时间
         */
        private String time;

        /**
         * 格式化时间
         */
        private String ftime;

        /**
         * 物流描述
         */
        private String context;

        /**
         * 地点
         */
        private String location;
    }
}
