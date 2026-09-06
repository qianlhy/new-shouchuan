package com.diy.controller.user;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONUtil;
import com.diy.enumeration.KuaidiStateEnum;
import com.diy.result.Result;
import com.diy.vo.Kuaidi100VO;
import com.google.gson.Gson;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

/**
 * 快递100查询Controller
 */
@RestController
@RequestMapping("/user/kuaidi")
@Api(tags = "快递查询接口")
@Slf4j
public class Kuaidi100Controller {

    @Value("${diy.kuaidi100.customer}")
    private String customer;

    @Value("${diy.kuaidi100.key}")
    private String key;

    private static final String QUERY_URL = "https://poll.kuaidi100.com/poll/query.do";

    /**
     * 计算MD5签名（使用UTF-8编码）
     */
    private String md5(String str) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] bytes = md.digest(str.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                String hex = Integer.toHexString(b & 0xFF);
                if (hex.length() == 1) {
                    sb.append("0");
                }
                sb.append(hex);
            }
            return sb.toString().toUpperCase();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5签名失败", e);
        }
    }

    /**
     * 查询快递物流轨迹
     *
     * @param com 快递公司编码
     * @param num 快递单号
     * @param phone 收件人手机号（中通查询需要）
     * @return 物流信息
     */
    @GetMapping("/query")
    @ApiOperation("查询快递物流信息")
    public Result<Kuaidi100VO> queryTrack(
            @ApiParam("快递公司编码，如：yuantong、sf、sto等，不传则默认中通") @RequestParam(required = false) String com,
            @ApiParam("快递单号") @RequestParam String num,
            @ApiParam("收件人手机号") @RequestParam(required = false) String phone) {
        log.info("查询快递物流信息：com={}, num={}, phone={}", com, num, phone);

        try {
            // 构建param参数
            Map<String, Object> paramMap = new HashMap<>();
            // 默认使用中通快递，只对接了中通
            paramMap.put("com", com != null && !com.isEmpty() ? com : "zhongtong");
            paramMap.put("num", num);
            // 中通查询需要手机号，取后11位（或全部）
            String phoneParam = phone != null && !phone.isEmpty() ? phone : "";
            paramMap.put("phone", phoneParam); // 收件人或寄件人手机号
            paramMap.put("from", ""); // 出发地城市
            paramMap.put("to", ""); // 目的地城市
            paramMap.put("resultv2", "0"); // 返回数据格式
            paramMap.put("show", "0"); // 返回格式
            paramMap.put("order", "desc"); // 排序方式

            String param = new Gson().toJson(paramMap);
            log.info("快递100请求参数：param={}", param);

            // 计算签名: MD5(param + key + customer)
            String signStr = param + key + customer;
            String sign = md5(signStr);
            log.info("快递100签名：sign={}", sign);

            // 构建请求参数
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("customer", customer);
            requestBody.put("sign", sign);
            requestBody.put("param", param);

            // 发送POST请求
            HttpResponse response = HttpRequest.post(QUERY_URL)
                    .form(requestBody)
                    .timeout(30000)
                    .execute();

            String body = response.body();
            log.info("快递100查询结果：{}", body);

            // 解析结果
            Kuaidi100VO kuaidiInfo = JSONUtil.toBean(body, Kuaidi100VO.class);

            // 设置状态描述
            if (kuaidiInfo.getState() != null) {
                try {
                    Integer stateCode = Integer.parseInt(kuaidiInfo.getState());
                    kuaidiInfo.setStateDesc(KuaidiStateEnum.getDescByCode(stateCode));
                    kuaidiInfo.setIsSign(stateCode == 3); // 3表示已签收
                } catch (NumberFormatException e) {
                    log.warn("解析状态码失败：{}", kuaidiInfo.getState());
                }
            }

            return Result.success(kuaidiInfo);

        } catch (Exception e) {
            log.error("查询快递物流信息失败：com={}, num={}", com, num, e);
            return Result.error("查询快递物流信息失败：" + e.getMessage());
        }
    }

    /**
     * 获取支持的快递公司列表
     *
     * @return 快递公司列表
     */
    @GetMapping("/companies")
    @ApiOperation("获取支持的快递公司列表")
    public Result<Map<String, String>> getCompanies() {
        Map<String, String> companies = new HashMap<>();
        companies.put("sf", "顺丰速运");
        companies.put("yuantong", "圆通速递");
        companies.put("sto", "申通快递");
        companies.put("yunda", "韵达快递");
        companies.put("jd", "京东物流");
        companies.put("ems", "EMS");
        companies.put("zto", "中通快递");
        companies.put("tiantian", "天天快递");
        companies.put("debang", "德邦快递");
        companies.put("jtexpress", "极兔速递");
        companies.put("youzhengguonei", "邮政国内");
        companies.put("danniao", "丹鸟物流");
        companies.put("best", "百世快递");
        companies.put("suning", "苏宁物流");
        companies.put("annengwuliu", "安能物流");
        return Result.success(companies);
    }
}
