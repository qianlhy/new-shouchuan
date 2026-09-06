package com.diy.service;

import com.diy.dto.*;
import com.diy.result.PageResult;
import com.diy.vo.OrderCreateVO;
import com.diy.vo.OrderPaymentStatusVO;
import com.diy.vo.OrderPaymentVO;
import com.diy.vo.OrderSubmitVO;
import com.diy.vo.OrderVO;

public interface OrderService {
    /**
     * 用户下单
     */
    OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO);
    
    /**
     * 从购物车创建订单
     */
    OrderCreateVO createOrderFromCart(OrdersSubmitDTO ordersSubmitDTO);
    
    /**
     * 订单支付
     */
    OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception;

    /**
     * 支付成功，修改订单状态
     * @param outTradeNo 商户订单号
     * @param transactionId 微信支付交易号
     */
    void paySuccess(String outTradeNo, String transactionId);

    /**
     * 用户端分页查询订单
     */
    PageResult pageQuery4User(OrdersPageQueryDTO ordersPageQueryDTO);
    
    /**
     * 分页查询订单列表（新接口）
     */
    PageResult listOrders(OrdersPageQueryDTO ordersPageQueryDTO);

    /**
     * 查看订单详情
     */
    OrderVO getDetails(Long id);

    /**
     * 取消订单
     */
    void update(Long id);

    /**
     * 再来一单
     */
    void repetition(Long id);

    /**
     * 管理端条件查询订单
     */
    PageResult conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO);

    /**
     * 发货
     */
    void delivery(Long id);

    /**
     * 完成订单
     */
    void complete(Long id);

    /**
     * 取消订单
     */
    void cancel(OrdersCancelDTO ordersCancelDTO);

    /**
     * 用户催单
     */
    void reminder(Long id);
    
    /**
     * 更新订单状态
     * @param orderStatusUpdateDTO 订单状态更新信息
     */
    void updateStatus(OrderStatusUpdateDTO orderStatusUpdateDTO);
    
    /**
     * 查询订单支付状态
     * @param orderNo 订单号
     * @return 订单支付状态信息
     */
    OrderPaymentStatusVO getPaymentStatus(String orderNo);
    
    /**
     * 申请退款
     * @param orderId 订单ID
     */
    void refund(Long orderId) throws Exception;

    /**
     * 取消退款申请
     * @param orderId 订单ID
     */
    void cancelRefund(Long orderId);

    /**
     * 退款成功回调，更新订单状态并回滚库存
     * @param outTradeNo 商户订单号
     */
    void refundSuccess(String outTradeNo);
    
    /**
     * 管理员审核通过退款，实际执行微信退款
     * @param orderId 订单ID
     * @param adminPhone 管理员手机号（用于验证）
     */
    void adminRefund(Long orderId, String adminPhone) throws Exception;

    /**
     * 修改订单地址
     * @param orderId 订单ID
     * @param addressDTO 地址信息
     */
    void updateOrderAddress(Long orderId, OrderAddressUpdateDTO addressDTO);

    /**
     * 管理员修改订单备注
     * @param orderId 订单ID
     * @param remark 备注内容
     */
    void updateOrderRemark(Long orderId, String remark);
}