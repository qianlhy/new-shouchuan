package com.diy.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.diy.constant.MessageConstant;
import com.diy.context.BaseContext;
import com.diy.dto.*;
import com.diy.entity.*;
import com.diy.exception.OrderBusinessException;
import com.diy.exception.ShoppingCartBusinessException;
import com.diy.mapper.*;
import com.diy.result.PageResult;
import com.diy.service.CartItemService;
import com.diy.service.OrderService;
import com.diy.utils.WeChatPayUtil;
import com.diy.utils.WxCloudStorageUtil;
import com.diy.vo.OrderCreateVO;
import com.diy.vo.OrderPaymentStatusVO;
import com.diy.vo.OrderPaymentVO;
import com.diy.vo.OrderSubmitVO;
import com.diy.vo.OrderVO;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
//todo 下单没有扣库存
@Slf4j
@Service
public class OrderServiceImpl implements OrderService {
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private ProductMapper productMapper;
    @Autowired
    private WeChatPayUtil weChatPayUtil;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private CartItemService cartItemService;
    @Autowired
    private DiyMaterialMapper diyMaterialMapper; // 新增：用于材料库存操作
    @Autowired
    private WxCloudStorageUtil wxCloudStorageUtil; // 用于删除DIY设计图片

    @Value("${dev.mock-payment:false}")
    private Boolean mockPayment;

    @Transactional
    @Override
    public OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO) {
        Long userId = BaseContext.getCurrentId();

        CartItem cartItemQuery = CartItem.builder()
                .userId(userId)
                .build();
        List<CartItem> cartItems = shoppingCartMapper.list(cartItemQuery);

        if (cartItems == null || cartItems.isEmpty()) {
            throw new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }

        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        for (CartItem cartItem : cartItems) {
            Long productId = cartItem.getProductId();
            Integer quantity = cartItem.getQuantity();

            // 判断是否是DIY商品（productId为负数）
            boolean isDiy = productId != null && productId < 0;

            if (isDiy) {
                // DIY商品：从diyData中解析信息
                String diyData = cartItem.getDiyData();
                if (diyData == null || diyData.isEmpty()) {
                    throw new OrderBusinessException("DIY商品数据缺失");
                }

                try {
                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode root = mapper.readTree(diyData);

                    String title = root.has("title") ? root.get("title").asText() : "DIY设计";
                    BigDecimal price = root.has("price") ? new BigDecimal(root.get("price").asText()) : BigDecimal.ZERO;
                    String imageUrl = root.has("imageUrl") ? root.get("imageUrl").asText() : "";

                    BigDecimal itemAmount = price.multiply(new BigDecimal(quantity));
                    totalAmount = totalAmount.add(itemAmount);

                    OrderItem orderItem = OrderItem.builder()
                            .productId(productId)
                            .title(title)
                            .price(price)
                            .quantity(quantity)
                            .productImage(imageUrl)
                            .diyData(diyData)  // 保存DIY数据
                            .build();
                    orderItems.add(orderItem);
                } catch (Exception e) {
                    log.error("解析DIY数据失败", e);
                    throw new OrderBusinessException("DIY商品数据解析失败");
                }
            } else {
                // 普通商品：查询product表
                Product product = productMapper.getById(productId);
                if (product == null) {
                    throw new OrderBusinessException(MessageConstant.PRODUCT_NOT_FOUND);
                }

                // 检查库存是否充足
                if (product.getStock() == null || product.getStock() < quantity) {
                    throw new OrderBusinessException("商品【" + product.getTitle() + "】库存不足，当前库存：" +
                        (product.getStock() == null ? 0 : product.getStock()));
                }

                // 扣减库存
                product.setStock(product.getStock() - quantity);
                productMapper.update(product);

                BigDecimal itemAmount = product.getPrice().multiply(new BigDecimal(quantity));
                totalAmount = totalAmount.add(itemAmount);

                OrderItem orderItem = OrderItem.builder()
                        .productId(productId)
                        .title(product.getTitle())
                        .price(product.getPrice())
                        .quantity(quantity)
                        .productImage(product.getCoverImage())
                        .diyData(null)  // 普通商品没有DIY数据
                        .build();
                orderItems.add(orderItem);
            }
        }

        String orderNo = "ORD" + System.currentTimeMillis();

        // 订单封面图：取第一个商品的封面图（用于订单列表展示）
        String orderCoverImage = null;
        if (orderItems != null && !orderItems.isEmpty()) {
            orderCoverImage = orderItems.get(0).getProductImage();
        }

        Orders order = Orders.builder()
                .userId(userId)
                .orderNo(orderNo)
                .amount(totalAmount)
                .status(Orders.PENDING_PAYMENT)
                .createTime(LocalDateTime.now())
                .productImage(orderCoverImage)
            .remark(ordersSubmitDTO != null ? ordersSubmitDTO.getRemark() : null)
                .build();

        orderMapper.insert(order);
        Long orderId = order.getId();

        for (OrderItem orderItem : orderItems) {
            orderItem.setOrderId(orderId);
        }
        orderDetailMapper.insertBatch(orderItems);

        shoppingCartMapper.deleteByUserId(userId);

        return OrderSubmitVO.builder()
                .id(orderId)
                .orderNumber(orderNo)
                .orderAmount(totalAmount)
                .orderTime(order.getCreateTime())
                .build();
    }

    @Transactional
    @Override
    public OrderCreateVO createOrderFromCart(OrdersSubmitDTO ordersSubmitDTO) {
        Long userId = BaseContext.getCurrentId();
        log.info("开始从购物车创建订单，用户ID: {}", userId);

        CartItem cartItemQuery = CartItem.builder()
                .userId(userId)
                .build();
        List<CartItem> cartItems = shoppingCartMapper.list(cartItemQuery);
        log.info("购物车商品数量: {}", cartItems != null ? cartItems.size() : 0);

        if (cartItems == null || cartItems.isEmpty()) {
            System.out.println(cartItems == null);
            System.out.println(cartItems.isEmpty());
            throw new OrderBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }

        // 勾选结算：只处理选中的购物车项
        java.util.Set<Long> selectedIdSet = null;
        if (ordersSubmitDTO != null && ordersSubmitDTO.getCartItemIds() != null
                && !ordersSubmitDTO.getCartItemIds().isEmpty()) {
            selectedIdSet = new java.util.HashSet<>(ordersSubmitDTO.getCartItemIds());
            final java.util.Set<Long> idFilter = selectedIdSet;
            cartItems = cartItems.stream()
                    .filter(ci -> ci.getId() != null && idFilter.contains(ci.getId()))
                    .collect(java.util.stream.Collectors.toList());
            if (cartItems.isEmpty()) {
                throw new OrderBusinessException("请选择要结算的商品");
            }
            log.info("勾选结算商品数量: {}", cartItems.size());
        }

        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        for (CartItem cartItem : cartItems) {
            Long productId = cartItem.getProductId();
            Integer quantity = cartItem.getQuantity();
            log.info("处理购物车商品: productId={}, quantity={}", productId, quantity);

            // 判断是否是DIY商品（productId为负数）
            boolean isDiy = productId != null && productId < 0;

            if (isDiy) {
                // DIY商品：从diyData中解析信息
                String diyData = cartItem.getDiyData();
                if (diyData == null || diyData.isEmpty()) {
                    throw new OrderBusinessException("DIY商品数据缺失");
                }

                try {
                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode root = mapper.readTree(diyData);

                    String title = root.has("title") ? root.get("title").asText() : "DIY设计";
                    BigDecimal price = root.has("price") ? new BigDecimal(root.get("price").asText()) : BigDecimal.ZERO;
                    String imageUrl = root.has("imageUrl") ? root.get("imageUrl").asText() : "";

                    BigDecimal itemAmount = price.multiply(new BigDecimal(quantity));
                    totalAmount = totalAmount.add(itemAmount);

                    OrderItem orderItem = OrderItem.builder()
                            .productId(productId)
                            .title(title)
                            .price(price)
                            .quantity(quantity)
                            .productImage(imageUrl)
                            .diyData(diyData)  // 保存DIY数据
                            .build();
                    orderItems.add(orderItem);
                    log.info("添加DIY订单项: title={}, price={}, quantity={}", title, price, quantity);
                } catch (Exception e) {
                    log.error("解析DIY数据失败", e);
                    throw new OrderBusinessException("DIY商品数据解析失败");
                }
            } else {
                // 普通商品：查询product表
                Product product = productMapper.getById(productId);
                if (product == null) {
                    throw new OrderBusinessException(MessageConstant.PRODUCT_NOT_FOUND);
                }

                // 检查库存是否充足
                if (product.getStock() == null || product.getStock() < quantity) {
                    throw new OrderBusinessException("商品【" + product.getTitle() + "】库存不足，当前库存：" +
                        (product.getStock() == null ? 0 : product.getStock()));
                }

                // 扣减库存
                product.setStock(product.getStock() - quantity);
                productMapper.update(product);

                BigDecimal itemAmount = product.getPrice().multiply(new BigDecimal(quantity));
                totalAmount = totalAmount.add(itemAmount);

                OrderItem orderItem = OrderItem.builder()
                        .productId(productId)
                        .title(product.getTitle())
                        .price(product.getPrice())
                        .quantity(quantity)
                        .productImage(product.getCoverImage())
                        .diyData(null)  // 普通商品没有DIY数据
                        .build();
                orderItems.add(orderItem);
                log.info("添加订单项: title={}, price={}, quantity={}", product.getTitle(), product.getPrice(), quantity);
            }
        }

        String orderNo = "ORD" + System.currentTimeMillis();
        
        // 加上运费（如果有）
        BigDecimal shippingFee = BigDecimal.ZERO;
        if (ordersSubmitDTO != null && ordersSubmitDTO.getShippingFee() != null) {
            shippingFee = ordersSubmitDTO.getShippingFee();
        }
        BigDecimal finalAmount = totalAmount.add(shippingFee);
        log.info("生成订单号: {}, 商品金额: {}, 运费: {}, 总金额: {}", orderNo, totalAmount, shippingFee, finalAmount);

        // 订单封面图：取第一个商品的封面图（用于订单列表展示）
        String orderCoverImage = null;
        if (orderItems != null && !orderItems.isEmpty()) {
            orderCoverImage = orderItems.get(0).getProductImage();
        }

        Orders order = Orders.builder()
                .userId(userId)
                .orderNo(orderNo)
                .amount(finalAmount)
                .status(Orders.PENDING_PAYMENT)
                .createTime(LocalDateTime.now())
                .productImage(orderCoverImage)
                .remark(ordersSubmitDTO != null ? ordersSubmitDTO.getRemark() : null)
                // 添加收件人信息（如果直接传递）
                .receiverName(ordersSubmitDTO != null ? ordersSubmitDTO.getReceiverName() : null)
                .receiverPhone(ordersSubmitDTO != null ? ordersSubmitDTO.getReceiverPhone() : null)
                .receiverProvince(ordersSubmitDTO != null ? ordersSubmitDTO.getReceiverProvince() : null)
                .receiverCity(ordersSubmitDTO != null ? ordersSubmitDTO.getReceiverCity() : null)
                .receiverDistrict(ordersSubmitDTO != null ? ordersSubmitDTO.getReceiverDistrict() : null)
                .receiverDetail(ordersSubmitDTO != null ? ordersSubmitDTO.getReceiverDetail() : null)
                .build();

        orderMapper.insert(order);
        Long orderId = order.getId();
        log.info("订单插入成功，orderId: {}", orderId);

        // 设置订单ID
        log.info("准备插入订单详情，订单项数量: {}", orderItems.size());
        for (OrderItem orderItem : orderItems) {
            orderItem.setOrderId(orderId);
            log.info("设置订单项orderId: {}, productId: {}, title: {}", 
                orderId, orderItem.getProductId(), orderItem.getTitle());
        }
        
        // 批量插入订单详情
        try {
            log.info("开始执行 insertBatch，参数数量: {}", orderItems.size());
            orderDetailMapper.insertBatch(orderItems);
            log.info("订单详情插入成功");
        } catch (Exception e) {
            log.error("订单详情插入失败", e);
            throw e;
        }

        // 勾选结算只删除已下单项，保留未勾选；否则清空整车
        if (selectedIdSet != null) {
            for (CartItem ordered : cartItems) {
                if (ordered.getId() != null) {
                    shoppingCartMapper.deleteById(ordered.getId());
                }
            }
            log.info("勾选结算后删除已结算项数量: {}", cartItems.size());
        } else {
            shoppingCartMapper.deleteByUserId(userId);
            log.info("清空购物车成功");
        }

        OrderCreateVO.Order orderVO = OrderCreateVO.Order.builder()
                .orderId(orderId)
                .orderNo(orderNo)
                .amount(totalAmount)
                .status(Orders.PENDING_PAYMENT)
                .build();

        return OrderCreateVO.builder()
                .order(orderVO)
                .build();
    }

    @Override
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        Long userId = BaseContext.getCurrentId();
        log.info("开始处理支付，用户ID: {}, 订单号: {}", userId, ordersPaymentDTO.getOrderNumber());

        User user = userMapper.getById(userId);
        if (user == null) {
            log.error("用户不存在，用户ID: {}", userId);
            throw new RuntimeException("用户不存在");
        }

        log.info("用户信息: id={}, nickname={}, openid={}", user.getId(), user.getNickname(),
                user.getOpenid() != null ? user.getOpenid() : "null");

        Orders ordersDB = orderMapper.getByNumberAndUserId(ordersPaymentDTO.getOrderNumber(), userId);
        if (ordersDB == null) {
            log.error("订单不存在，订单号: {}, 用户ID: {}", ordersPaymentDTO.getOrderNumber(), userId);
            throw new RuntimeException("订单不存在");
        }

        log.info("订单信息: id={}, orderNo={}, amount={}, status={}", ordersDB.getId(), ordersDB.getOrderNo(),
                ordersDB.getAmount(), ordersDB.getStatus());
        
        // 检查订单状态，只有待支付的订单才能支付
        if (!Orders.PENDING_PAYMENT.equals(ordersDB.getStatus())) {
            String statusText = getStatusText(ordersDB.getStatus());
            log.error("订单状态不允许支付，订单号: {}, 当前状态: {} ({})", 
                ordersPaymentDTO.getOrderNumber(), ordersDB.getStatus(), statusText);
            throw new OrderBusinessException("订单状态为【" + statusText + "】，无法支付");
        }
        
        // 若已存在 prepayId：允许复用，并重新生成小程序调起支付参数（用户在微信支付页取消后再次支付的场景）
        if (ordersDB.getPrepayId() != null && !ordersDB.getPrepayId().isEmpty()) {
            log.info("订单已存在prepayId，复用并重新签名，订单号: {}, prepayId: {}",
                    ordersDB.getOrderNo(), ordersDB.getPrepayId());

            JSONObject payParams = weChatPayUtil.buildJsapiPayParamsFromPrepayId(ordersDB.getPrepayId());
            String packageStr = payParams.getString("package");

            return OrderPaymentVO.builder()
                    .nonceStr(payParams.getString("nonceStr"))
                    .paySign(payParams.getString("paySign"))
                    .timeStamp(payParams.getString("timeStamp"))
                    .signType(payParams.getString("signType"))
                    .packageStr(packageStr)
                    .build();
        }

        // 模拟支付模式（开发测试用）
        if (Boolean.TRUE.equals(mockPayment)) {
            log.info("【开发模式】使用模拟支付");

            OrderPaymentVO vo = OrderPaymentVO.builder()
                    .nonceStr("test_nonce_str")
                    .paySign("test_pay_sign")
                    .timeStamp(String.valueOf(System.currentTimeMillis() / 1000))
                    .signType("RSA")
                    .packageStr("prepay_id=test_prepay_id")
                    .build();

            // 直接更新订单状态为已支付
            LocalDateTime now = LocalDateTime.now();
            Orders orders = Orders.builder()
                    .id(ordersDB.getId())
                    .status(Orders.PAID)
                    .payTime(now)
                    .updateTime(now)
                    .transactionId("MOCK-" + ordersDB.getOrderNo())
                    .build();
            orderMapper.update(orders);

            log.info("模拟支付完成");
            return vo;
        }

        // 真实微信支付模式
        if (user.getOpenid() == null || user.getOpenid().isEmpty()) {
            log.error("用户openid为空，无法发起微信支付");
            throw new RuntimeException("用户未绑定微信，请重新登录");
        }

        // 构建商品描述信息（符合微信要求）
        List<OrderItem> orderItems = orderDetailMapper.getByOrderId(ordersDB.getId());
        String description = "商品购买";
        if (orderItems != null && !orderItems.isEmpty()) {
            // 取第一个商品作为主要描述
            String firstItemTitle = orderItems.get(0).getTitle();
            if (orderItems.size() > 1) {
                description = firstItemTitle + "等" + orderItems.size() + "件商品";
            } else {
                description = firstItemTitle;
            }
            // 限制长度（微信要求description不超过127字符）
            if (description.length() > 127) {
                description = description.substring(0, 124) + "...";
            }
        }
        log.info("订单商品描述: {}", description);

        try {
            log.info("开始调用微信支付接口...");
            JSONObject jsonObject = weChatPayUtil.pay(
                    ordersPaymentDTO.getOrderNumber(),
                    ordersDB.getAmount(),
                    description,
                    user.getOpenid()
            );

            log.info("微信支付接口调用成功，返回数据: {}", jsonObject);
            
            // 提取 prepay_id 并保存到数据库（防止用户取消后重复调用）
            String packageStr = jsonObject.getString("package");
            if (packageStr != null && packageStr.startsWith("prepay_id=")) {
                String prepayId = packageStr.substring(10); // 提取 "prepay_id=" 后面的内容
                log.info("提取到 prepay_id: {}", prepayId);
                
                // 立即保存 prepay_id 到数据库
                Orders updateOrder = Orders.builder()
                        .id(ordersDB.getId())
                        .prepayId(prepayId)
                        .updateTime(LocalDateTime.now())
                        .build();
                orderMapper.update(updateOrder);
                log.info("已保存 prepay_id 到数据库，订单ID: {}", ordersDB.getId());
            }

            return OrderPaymentVO.builder()
                    .nonceStr(jsonObject.getString("nonceStr"))
                    .paySign(jsonObject.getString("paySign"))
                    .timeStamp(jsonObject.getString("timeStamp"))
                    .signType(jsonObject.getString("signType"))
                    .packageStr(packageStr)
                    .build();
        } catch (Exception e) {
            log.error("微信支付调用失败", e);
            throw new RuntimeException("微信支付调用失败: " + e.getMessage(), e);
        }
    }

    @Transactional
    @Override
    public void paySuccess(String outTradeNo, String transactionId) {
        log.info("处理支付成功回调，订单号: {}", outTradeNo);

        Orders ordersDB = orderMapper.getByNumber(outTradeNo);
        if (ordersDB == null) {
            log.error("订单不存在，订单号: {}", outTradeNo);
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }

        if (Orders.PAID.equals(ordersDB.getStatus()) ||
                Orders.SHIPPED.equals(ordersDB.getStatus()) ||
                Orders.COMPLETED.equals(ordersDB.getStatus())) {
            log.warn("订单已处于支付后状态，忽略本次回调，订单号: {}, 当前状态: {}", outTradeNo, ordersDB.getStatus());
            return;
        }

        if (!Orders.PENDING_PAYMENT.equals(ordersDB.getStatus())) {
            log.error("订单状态不正确，无法支付，订单号: {}, 当前状态: {}", outTradeNo, ordersDB.getStatus());
            throw new OrderBusinessException("订单状态异常，无法完成支付");
        }

        LocalDateTime now = LocalDateTime.now();
        Orders orders = Orders.builder()
                .id(ordersDB.getId())
                .status(Orders.PAID)
                .payTime(now)
                .updateTime(now)
                .transactionId(transactionId)
                .prepayId(null) // 清空 prepayId
                .build();
        orderMapper.update(orders);

        log.info("支付成功回调处理完成，订单号: {}, 微信交易号: {}", outTradeNo, transactionId);
    }

    @Override
    public PageResult pageQuery4User(OrdersPageQueryDTO ordersPageQueryDTO) {
        PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());
        Page<Orders> page = orderMapper.pageQuery(ordersPageQueryDTO);

        List<OrderVO> list = new ArrayList<>();
        if (page != null && page.getTotal() > 0) {
            for (Orders orders : page) {
                Long orderId = orders.getId();
                List<OrderItem> orderItems = orderDetailMapper.getByOrderId(orderId);

                OrderVO orderVO = new OrderVO();
                BeanUtils.copyProperties(orders, orderVO);
                orderVO.setOrderItems(orderItems);
                list.add(orderVO);
            }
        }
        return new PageResult(page.getTotal(), list);
    }

    @Override
    public PageResult listOrders(OrdersPageQueryDTO ordersPageQueryDTO) {
        Long userId = BaseContext.getCurrentId();
        ordersPageQueryDTO.setUserId(userId);

        PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());
        Page<Orders> page = orderMapper.pageQuery(ordersPageQueryDTO);

        return new PageResult(page.getTotal(), page.getResult());
    }

    @Override
    public PageResult conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO) {
        PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());
        Page<Orders> page = orderMapper.pageQuery(ordersPageQueryDTO);

        List<OrderVO> orderVOList = new ArrayList<>();
        List<Orders> ordersList = page.getResult();
        
        if (!CollectionUtils.isEmpty(ordersList)) {
            for (Orders orders : ordersList) {
                OrderVO orderVO = new OrderVO();
                BeanUtils.copyProperties(orders, orderVO);
                
                List<OrderItem> orderItems = orderDetailMapper.getByOrderId(orders.getId());
                orderVO.setOrderItems(orderItems);
                orderVOList.add(orderVO);
            }
        }
        return new PageResult(page.getTotal(), orderVOList);
    }

    @Override
    public void delivery(Long id) {
        Orders order = new Orders();
        order.setId(id);
        order.setStatus(Orders.SHIPPED);
        order.setDeliveryTime(LocalDateTime.now()); // 设置发货时间
        orderMapper.update(order);
    }

    @Override
    public void complete(Long id) {
        Orders order = new Orders();
        order.setStatus(Orders.COMPLETED);
        order.setId(id);
        orderMapper.update(order);
    }

    @Override
    public void cancel(OrdersCancelDTO ordersCancelDTO) {
        Long id = ordersCancelDTO.getId();
        log.info("管理端取消订单，订单ID: {}", id);
        
        // 查询订单状态
        Orders order = orderMapper.getDetailsById(id);
        if (order == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        
        // 只有待支付的订单才能取消
        if (!Orders.PENDING_PAYMENT.equals(order.getStatus())) {
            throw new OrderBusinessException("订单状态不允许取消");
        }
        
        // 回滚库存
        rollbackOrderStock(id);
        
        // 更新订单状态为已取消
        Orders updateOrder = new Orders();
        updateOrder.setId(id);
        updateOrder.setStatus(Orders.CANCELLED); // 设置为已取消状态
        updateOrder.setPrepayId(null); // 清空 prepayId
        updateOrder.setUpdateTime(LocalDateTime.now());
        orderMapper.update(updateOrder);
        
        log.info("管理端取消订单成功，库存已回滚，状态已更新为已取消，订单ID: {}", id);
    }

    @Override
    public void repetition(Long id) {
        // 再来一单：将原订单的商品重新加入当前用户的购物车
        Long userId = BaseContext.getCurrentId();
        Orders order = orderMapper.getDetailsById(id);
        if (order == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        if (!userId.equals(order.getUserId())) {
            throw new OrderBusinessException("无权操作该订单");
        }
        List<OrderItem> orderItems = orderDetailMapper.getByOrderId(id);
        if (orderItems == null || orderItems.isEmpty()) {
            return;
        }
        for (OrderItem orderItem : orderItems) {
            com.diy.dto.AddToCartDTO dto = new com.diy.dto.AddToCartDTO();
            dto.setProductId(orderItem.getProductId());
            dto.setQuantity(orderItem.getQuantity());
            cartItemService.addToCart(dto);
        }
    }

    @Override
    public void reminder(Long id) {
        Orders order = orderMapper.getDetailsById(id);
        if (order == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        // 暂不做实际推送，仅记录日志，预留扩展点
        log.info("用户催单，订单ID: {}，订单号: {}", order.getId(), order.getOrderNo());
    }
    
    @Override
    public void updateStatus(OrderStatusUpdateDTO orderStatusUpdateDTO) {
        Long orderId = orderStatusUpdateDTO.getOrderId();
        Integer status = orderStatusUpdateDTO.getStatus();
        
        Orders order = orderMapper.getDetailsById(orderId);
        if (order == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        
        Orders updateOrder = Orders.builder()
                .id(orderId)
                .status(status)
                .build();
                
        // 如果是发货状态，设置发货时间和运单号
        if (Orders.SHIPPED.equals(status)) {
            updateOrder.setDeliveryTime(LocalDateTime.now());
            // 如果提供了运单号，则更新运单号
            if (orderStatusUpdateDTO.getTrackingNumber() != null && 
                !orderStatusUpdateDTO.getTrackingNumber().isEmpty()) {
                updateOrder.setTrackingNumber(orderStatusUpdateDTO.getTrackingNumber());
            }
        }
        
        orderMapper.update(updateOrder);
    }

    @Override
    public OrderVO getDetails(Long id) {
        Orders orders = orderMapper.getDetailsById(id);
        List<OrderItem> orderItems = orderDetailMapper.getByOrderId(id);

        OrderVO orderVO = new OrderVO();
        BeanUtils.copyProperties(orders, orderVO);
        orderVO.setOrderItems(orderItems);

        // 兼容历史数据：如果订单主图为空，则使用订单项中的第一张商品图作为主图
        if ((orderVO.getProductImage() == null || orderVO.getProductImage().isEmpty())
                && orderItems != null && !orderItems.isEmpty()) {
            for (OrderItem item : orderItems) {
                if (item != null && item.getProductImage() != null && !item.getProductImage().isEmpty()) {
                    orderVO.setProductImage(item.getProductImage());
                    break;
                }
            }
        }
        return orderVO;
    }

    @Transactional
    @Override
    public void update(Long id) {
        log.info("取消订单，订单ID: {}", id);
        
        // 查询订单状态
        Orders order = orderMapper.getDetailsById(id);
        if (order == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        
        // 只有待支付的订单才能取消
        if (!Orders.PENDING_PAYMENT.equals(order.getStatus())) {
            throw new OrderBusinessException("订单状态不允许取消");
        }
        
        // 回滚库存
        rollbackOrderStock(id);
        
        // 如果是DIY订单且有设计图片，则删除设计图片
        if (order.getDiyName() != null && !order.getDiyName().isEmpty() && 
            order.getProductImage() != null && !order.getProductImage().isEmpty()) {
            try {
                wxCloudStorageUtil.delete(order.getProductImage());
                log.info("DIY设计图片删除成功: {}", order.getProductImage());
            } catch (Exception e) {
                log.warn("DIY设计图片删除失败: {}", order.getProductImage(), e);
            }
        }
        
        // 更新订单状态为已取消
        Orders updateOrder = new Orders();
        updateOrder.setId(id);
        updateOrder.setStatus(Orders.CANCELLED); // 设置为已取消状态
        updateOrder.setPrepayId(null); // 清空 prepayId
        updateOrder.setUpdateTime(LocalDateTime.now());
        orderMapper.update(updateOrder);
        
        log.info("订单取消成功，库存已回滚，状态已更新为已取消，订单ID: {}", id);
    }
    
    @Override
    public OrderPaymentStatusVO getPaymentStatus(String orderNo) {
        // 获取当前用户ID
        Long userId = BaseContext.getCurrentId();
        log.info("查询订单支付状态，用户ID: {}, 订单号: {}", userId, orderNo);
        
        // 根据订单号和用户ID查询订单（防止查询其他用户的订单）
        Orders order = orderMapper.getByNumberAndUserId(orderNo, userId);
        
        if (order == null) {
            throw new OrderBusinessException("订单不存在");
        }
        
        // 获取状态文本
        String statusText = getStatusText(order.getStatus());
        
        // 构建返回对象
        return OrderPaymentStatusVO.builder()
                .orderId(order.getId())
                .orderNo(order.getOrderNo())
                .status(order.getStatus())
                .amount(order.getAmount())
                .createTime(order.getCreateTime())
                .payTime(order.getPayTime())
                .transactionId(order.getTransactionId())
                .isPaid(order.getStatus() != null && order.getStatus() >= Orders.PAID)
                .statusText(statusText)
                .build();
    }
    
    /**
     * 获取订单状态文本
     */
    private String getStatusText(Integer status) {
        if (status == null) {
            return "未知状态";
        }
        switch (status) {
            case 0: // PENDING_PAYMENT
                return "待支付";
            case 1: // PAID
                return "已支付";
            case 2: // SHIPPED
                return "已发货";
            case 3: // COMPLETED
                return "已完成";
            case 4: // REFUNDING
                return "退款中";
            case 5: // REFUNDED
                return "已退款";
            case 6: // CANCELLED
                return "已取消";
            default:
                return "未知状态";
        }
    }
    
    @Transactional
    @Override
    public void refund(Long orderId) throws Exception {
        log.info("用户申请退款，订单ID: {}", orderId);
        
        // 获取当前用户ID
        Long userId = BaseContext.getCurrentId();
        log.info("用户端申请退款，用户ID: {}", userId);
        
        // 查询订单信息
        Orders order = orderMapper.getDetailsById(orderId);
        if (order == null) {
            log.error("订单不存在，订单ID: {}", orderId);
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        
        // 验证订单归属
        if (!userId.equals(order.getUserId())) {
            log.error("订单不属于当前用户，订单ID: {}, 用户ID: {}, 订单所属用户: {}", 
                orderId, userId, order.getUserId());
            throw new OrderBusinessException("无权操作该订单");
        }
        
        // 限制只有已支付未发货的订单才能申请退款
        if (!Orders.PAID.equals(order.getStatus())) {
            log.error("订单状态不允许退款，订单ID: {}, 当前状态: {}", orderId, order.getStatus());
            throw new OrderBusinessException("只有已支付未发货的订单才能申请退款");
        }
        
        // 检查是否已经退款
        if (Orders.REFUNDING.equals(order.getStatus()) || Orders.REFUNDED.equals(order.getStatus())) {
            log.warn("订单已退款或退款中，订单ID: {}, 状态: {}", orderId, order.getStatus());
            throw new OrderBusinessException("订单已退款或退款中");
        }
        
        // 更新订单状态为退款中，等待管理员审核
        LocalDateTime now = LocalDateTime.now();
        Orders updateOrder = Orders.builder()
            .id(orderId)
            .status(Orders.REFUNDING)
            .refundAmount(order.getAmount())
            .refundTime(now)
            .updateTime(now)
            .build();
        orderMapper.update(updateOrder);
        
        log.info("用户退款申请成功，订单ID: {}，状态已更新为退款中，等待管理员审核", orderId);
    }

    @Transactional
    @Override
    public void cancelRefund(Long orderId) {
        log.info("用户取消退款申请，订单ID: {}", orderId);

        // 获取当前用户ID
        Long userId = BaseContext.getCurrentId();
        log.info("用户端取消退款，用户ID: {}", userId);

        // 查询订单信息
        Orders order = orderMapper.getDetailsById(orderId);
        if (order == null) {
            log.error("订单不存在，订单ID: {}", orderId);
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }

        // 验证订单归属
        if (!userId.equals(order.getUserId())) {
            log.error("订单不属于当前用户，订单ID: {}, 用户ID: {}, 订单所属用户: {}",
                orderId, userId, order.getUserId());
            throw new OrderBusinessException("无权操作该订单");
        }

        // 限制只有退款中的订单才能取消退款
        if (!Orders.REFUNDING.equals(order.getStatus())) {
            log.error("订单状态不允许取消退款，订单ID: {}, 当前状态: {}", orderId, order.getStatus());
            throw new OrderBusinessException("只有退款中的订单才能取消退款申请");
        }

        // 检查管理员是否已实际发起退款（refund_id不为空表示已调用微信退款接口）
        if (order.getRefundId() != null && !order.getRefundId().isEmpty()) {
            log.error("管理员已发起退款，无法取消，订单ID: {}, refundId: {}", orderId, order.getRefundId());
            throw new OrderBusinessException("退款已处理，无法取消");
        }

        // 更新订单状态为已支付，清空退款相关信息
        LocalDateTime now = LocalDateTime.now();
        Orders updateOrder = Orders.builder()
            .id(orderId)
            .status(Orders.PAID)
            .refundAmount(null)
            .refundTime(null)
            .updateTime(now)
            .build();
        orderMapper.update(updateOrder);

        log.info("用户取消退款申请成功，订单ID: {}，状态已恢复为已支付", orderId);
    }

    @Transactional
    @Override
    public void refundSuccess(String outTradeNo) {
        log.info("处理退款成功回调，订单号: {}", outTradeNo);
        
        Orders order = orderMapper.getByNumber(outTradeNo);
        if (order == null) {
            log.error("订单不存在，订单号: {}", outTradeNo);
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        
        // 检查订单状态，避重复回调
        if (Orders.REFUNDED.equals(order.getStatus())) {
            log.warn("订单已退款，忽略本次回调，订单号: {}", outTradeNo);
            return;
        }
        
        // 回滚库存（事务中执行）
        rollbackOrderStock(order.getId());
        
        // 更新订单状态为已退款
        LocalDateTime now = LocalDateTime.now();
        Orders updateOrder = Orders.builder()
                .id(order.getId())
                .status(Orders.REFUNDED)
                .updateTime(now)
                .build();
        orderMapper.update(updateOrder);
        
        log.info("退款成功处理完成，订单ID: {}, 库存已回滚", order.getId());
    }
    
    @Transactional
    @Override
    public void adminRefund(Long orderId, String adminPhone) throws Exception {
        log.info("管理员审核通过退款，订单ID: {}，管理员手机号: {}", orderId, adminPhone);
        
        // 验证手机号
        if (!"13020695025".equals(adminPhone)) {
            log.error("管理员手机号验证失败，输入手机号: {}", adminPhone);
            throw new OrderBusinessException("管理员手机号验证失败");
        }
        
        // 查询订单信息
        Orders order = orderMapper.getDetailsById(orderId);
        if (order == null) {
            log.error("订单不存在，订单ID: {}", orderId);
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        
        // 验证订单状态（必须是退款中状态）
        if (!Orders.REFUNDING.equals(order.getStatus())) {
            log.error("订单状态不允许退款，订单ID: {}，当前状态: {}", orderId, order.getStatus());
            throw new OrderBusinessException("订单状态不允许退款");
        }
        
        // 生成退款单号
        String outRefundNo = "RF" + System.currentTimeMillis();
        
        log.info("订单信息: orderNo={}, amount={}, transactionId={}", 
            order.getOrderNo(), order.getAmount(), order.getTransactionId());
        log.info("退款单号: {}", outRefundNo);
        
        try {
            // 调用微信退款接口
            String refundResult = weChatPayUtil.refund(
                order.getOrderNo(),      // 商户订单号
                outRefundNo,             // 商户退款单号
                order.getAmount(),       // 退款金额（全额退款）
                order.getAmount()        // 原订单金额
            );
            
            log.info("微信退款接口调用成功，结果: {}", refundResult);
            
            // 解析退款结果
            JSONObject refundJson = JSON.parseObject(refundResult);
            String refundId = refundJson.getString("refund_id"); // 微信退款单号
            
            // 更新订单状态为退款中（保持退款中状态，等待微信回调完成退款）
            LocalDateTime now = LocalDateTime.now();
            Orders updateOrder = Orders.builder()
                .id(orderId)
                .refundId(refundId)  // 更新微信退款单号
                .updateTime(now)
                .build();
            orderMapper.update(updateOrder);
            
            log.info("管理员退款审核成功，订单ID: {}，退款单号: {}，微信退款ID: {}", 
                orderId, outRefundNo, refundId);
                
        } catch (Exception e) {
            log.error("管理员退款失败，订单ID: {}", orderId, e);
            throw new RuntimeException("管理员退款失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 回滚订单库存（通用方法）
     * @param orderId 订单ID
     */
    private void rollbackOrderStock(Long orderId) {
        log.info("开始回滚订单库存，订单ID: {}", orderId);
        
        // 查询订单信息，根据订单号前缀判断订单类型
        Orders order = orderMapper.getDetailsById(orderId);
        if (order == null) {
            log.warn("订单不存在，无法回滚库存，订单ID: {}", orderId);
            return;
        }
        
        // 根据订单号前缀判断类型：以"ORD"开头的是普通商品订单，以"DIY"开头的是DIY订单
        String orderNo = order.getOrderNo();
        boolean isDiyOrder = orderNo.startsWith("DIY");
        log.info("订单类型: {}, 订单号: {}", isDiyOrder ? "DIY订单" : "普通商品订单", orderNo);
        
        // 查询订单项
        List<OrderItem> orderItems = orderDetailMapper.getByOrderId(orderId);
        if (orderItems == null || orderItems.isEmpty()) {
            log.warn("订单无订单项，无需回滚库存，订单ID: {}", orderId);
            return;
        }
        
        for (OrderItem orderItem : orderItems) {
            Long productId = orderItem.getProductId();
            Integer quantity = orderItem.getQuantity();

            // 根据productId判断商品类型：负数表示DIY商品
            boolean isDiyProduct = productId != null && productId < 0;

            try {
                if (isDiyProduct) {
                    // DIY商品：跳过库存回滚（DIY商品没有库存概念）
                    log.info("DIY商品无需回滚库存: productId={}", productId);
                } else {
                    // 普通商品：回滚 product 表的库存
                    Product product = productMapper.getById(productId);
                    if (product != null) {
                        product.setStock(product.getStock() + quantity);
                        productMapper.update(product);
                        log.info("成功回滚普通商品库存: productId={}, quantity={}", productId, quantity);
                    } else {
                        log.warn("未找到商品，无法回滚库存: productId={}", productId);
                    }
                }
            } catch (Exception e) {
                log.error("回滚库存失败: productId={}, quantity={}, 商品类型={}",
                    productId, quantity, isDiyProduct ? "DIY" : "普通商品", e);
                throw new RuntimeException("回滚库存失败", e);
            }
        }
        
        log.info("订单库存回滚完成，订单ID: {}", orderId);
    }

    /**
     * 修改订单地址
     * 仅在订单未发货前可以修改（状态 0-待支付, 1-已支付）
     * @param orderId 订单ID
     * @param addressDTO 地址信息
     */
    @Override
    @Transactional
    public void updateOrderAddress(Long orderId, OrderAddressUpdateDTO addressDTO) {
        // 1. 查询订单
        Orders order = orderMapper.getDetailsById(orderId);
        if (order == null) {
            throw new OrderBusinessException("订单不存在");
        }

        // 2. 验证订单状态（只有待支付和已支付状态可以修改地址）
        Integer status = order.getStatus();
        if (status != 0 && status != 1) {
            throw new OrderBusinessException("订单已发货或已完成，无法修改地址");
        }

        // 3. 验证当前用户是否有权限修改（只能修改自己的订单）
        Long currentUserId = BaseContext.getCurrentId();
        if (!order.getUserId().equals(currentUserId)) {
            throw new OrderBusinessException("无权修改此订单");
        }

        // 4. 更新地址信息
        order.setReceiverName(addressDTO.getName());
        order.setReceiverPhone(addressDTO.getPhone());
        order.setReceiverProvince(addressDTO.getProvince());
        order.setReceiverCity(addressDTO.getCity());
        order.setReceiverDistrict(addressDTO.getDistrict());
        order.setReceiverDetail(addressDTO.getDetail());

        // 5. 保存更新
        orderMapper.update(order);
        log.info("订单地址修改成功，订单ID: {}", orderId);
    }

    /**
     * 管理员修改订单备注
     * @param orderId 订单ID
     * @param remark 备注内容
     */
    @Override
    @Transactional
    public void updateOrderRemark(Long orderId, String remark) {
        // 1. 查询订单
        Orders order = orderMapper.getDetailsById(orderId);
        if (order == null) {
            throw new OrderBusinessException("订单不存在");
        }

        // 2. 更新备注
        order.setRemark(remark);

        // 3. 保存更新
        orderMapper.update(order);
        log.info("订单备注修改成功，订单ID: {}", orderId);
    }
}
