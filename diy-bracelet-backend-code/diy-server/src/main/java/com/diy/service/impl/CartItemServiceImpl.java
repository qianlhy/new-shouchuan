package com.diy.service.impl;

import com.diy.context.BaseContext;
import com.diy.dto.AddToCartDTO;
import com.diy.dto.DeleteFromCartDTO;
import com.diy.dto.ShoppingCartDTO;
import com.diy.dto.UpdateDiyCartDTO;
import com.diy.entity.CartItem;
import com.diy.exception.BaseException;
import com.diy.mapper.ShoppingCartMapper;
import com.diy.service.CartItemService;
import com.diy.vo.CartItemListVO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CartItemServiceImpl implements CartItemService {
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    /**
     * 添加到购物车
     * @param shoppingCartDTO
     */
    @Override
    public void add(ShoppingCartDTO shoppingCartDTO) {
        //TODO: 实现添加到购物车逻辑
        // 1. 检查商品是否已在购物车中
        // 2. 如果存在，更新数量
        // 3. 如果不存在，插入新记录
    }
    
    /**
     * 添加商品到购物车（新接口）
     * @param addToCartDTO
     * @return
     */
    @Override
    public CartItem addToCart(AddToCartDTO addToCartDTO) {
        Long userId = BaseContext.getCurrentId();
        Long productId = addToCartDTO.getProductId();
        Integer quantity = addToCartDTO.getQuantity();
        String diyData = addToCartDTO.getDiyData();
        
        // 判断是否是DIY设计（productId为0或负数表示DIY设计）
        boolean isDiyDesign = productId == null || productId <= 0;

        if (isDiyDesign) {
            // DIY设计：每次添加都是新的记录，不合并
            // 使用负数作为product_id，确保唯一性（避免唯一索引冲突）
            long diyProductId = -(System.currentTimeMillis() * 1000L + (System.nanoTime() % 1000L));
            if (diyProductId >= 0) {
                diyProductId = -1L - (System.nanoTime() % 1_000_000_000L);
            }

            if (diyData == null || diyData.trim().isEmpty()) {
                throw new BaseException("DIY设计数据不能为空");
            }

            CartItem newItem = CartItem.builder()
                    .userId(userId)
                    .productId(diyProductId)
                    .quantity(quantity != null ? quantity : 1)
                    .diyData(diyData)
                    .createTime(LocalDateTime.now())
                    .build();
            shoppingCartMapper.insert(newItem);
            log.info("DIY加入购物车成功 userId={}, productId={}", userId, diyProductId);
            return newItem;
        }
        
        // 普通商品：检查是否已存在，存在则更新数量
        CartItem cartItemQuery = CartItem.builder()
                .userId(userId)
                .productId(productId)
                .build();
        
        List<CartItem> cartItems = shoppingCartMapper.list(cartItemQuery);
        
        CartItem cartItemResult;
        if (cartItems != null && !cartItems.isEmpty()) {
            // 如果存在，更新数量
            CartItem existingItem = cartItems.get(0);
            existingItem.setQuantity(existingItem.getQuantity() + quantity);
            shoppingCartMapper.updateQuantityById(existingItem);
            cartItemResult = existingItem;
        } else {
            // 如果不存在，插入新记录
            CartItem newItem = CartItem.builder()
                    .userId(userId)
                    .productId(productId)
                    .quantity(quantity)
                    .createTime(LocalDateTime.now())
                    .build();
            shoppingCartMapper.insert(newItem);
            cartItemResult = newItem;
        }
        
        return cartItemResult;
    }

    /**
     * 查看购物车
     */
    @Override
    public List<CartItem> list() {
        Long userId = BaseContext.getCurrentId();
        CartItem cartItem = CartItem.builder()
                .userId(userId)
                .build();
        return shoppingCartMapper.list(cartItem);
    }
    
    /**
     * 查看购物车（包含商品信息）
     */
    @Override
    public CartItemListVO listWithProductInfo() {
        Long userId = BaseContext.getCurrentId();
        List<Map<String, Object>> cartItems = shoppingCartMapper.listWithProductInfo(userId);
        if (cartItems == null || cartItems.isEmpty()) {
            return CartItemListVO.builder().items(java.util.Collections.emptyList()).build();
        }

        // 转换为VO（Map 取值需兼容 Integer/Long/BigInteger，避免强转失败导致整单列表空白）
        List<CartItemListVO.CartItem> items = cartItems.stream().map(map -> {
            CartItemListVO.CartItem item = new CartItemListVO.CartItem();
            item.setId(toLong(mapValue(map, "id")));
            Long productId = toLong(mapValue(map, "productId", "product_id"));
            item.setProductId(productId);
            item.setQuantity(toInteger(mapValue(map, "quantity")));

            String diyData = toStringValue(mapValue(map, "diyData", "diy_data"));
            boolean isDiy = (productId != null && productId < 0)
                    || (diyData != null && !diyData.trim().isEmpty());
            item.setIsDiy(isDiy);

            if (isDiy) {
                item.setDiyData(diyData);
                if (diyData != null && !diyData.isEmpty()) {
                    try {
                        ObjectMapper mapper = new ObjectMapper();
                        JsonNode root = mapper.readTree(diyData);

                        String title = root.has("title") ? root.get("title").asText() : "DIY设计";
                        BigDecimal price = root.has("price") ? new BigDecimal(root.get("price").asText()) : BigDecimal.ZERO;
                        String imageUrl = root.has("imageUrl") ? root.get("imageUrl").asText() : "";
                        String size = root.has("size") ? root.get("size").asText() : "";

                        item.setTitle(title);
                        item.setPrice(price);
                        item.setCoverImage(imageUrl);
                        item.setDiySize(size);
                    } catch (Exception e) {
                        log.error("解析DIY数据失败", e);
                        item.setTitle("DIY设计（解析失败）");
                        item.setPrice(BigDecimal.ZERO);
                        item.setCoverImage("");
                        item.setDiySize("");
                    }
                } else {
                    item.setTitle("DIY设计");
                    item.setPrice(BigDecimal.ZERO);
                    item.setCoverImage("");
                    item.setDiySize("");
                }
            } else {
                item.setTitle(toStringValue(mapValue(map, "title")));
                item.setPrice(toBigDecimal(mapValue(map, "price")));
                // 返回相对路径（如 /admin/common/image/...），由小程序 resolveImageUrl 拼当前域名
                // 不要再拼旧 OSS 域名 cloud.xiaotangstory.top
                String coverImage = toStringValue(mapValue(map, "coverImage", "cover_image"));
                item.setCoverImage(coverImage != null ? coverImage : "");
            }

            return item;
        }).collect(Collectors.toList());

        return CartItemListVO.builder()
                .items(items)
                .build();
    }

    private static Object mapValue(Map<String, Object> map, String... keys) {
        if (map == null || keys == null) {
            return null;
        }
        for (String key : keys) {
            if (key == null) continue;
            if (map.containsKey(key)) {
                return map.get(key);
            }
            // MyBatis 某些配置下 key 可能被转成全小写
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                if (entry.getKey() != null && entry.getKey().equalsIgnoreCase(key)) {
                    return entry.getValue();
                }
            }
        }
        return null;
    }

    private static Long toLong(Object value) {
        if (value == null) return null;
        if (value instanceof Long) return (Long) value;
        if (value instanceof Integer) return ((Integer) value).longValue();
        if (value instanceof Short) return ((Short) value).longValue();
        if (value instanceof java.math.BigInteger) return ((java.math.BigInteger) value).longValue();
        if (value instanceof BigDecimal) return ((BigDecimal) value).longValue();
        if (value instanceof Number) return ((Number) value).longValue();
        String text = String.valueOf(value).trim();
        if (text.isEmpty() || "null".equalsIgnoreCase(text)) return null;
        return Long.parseLong(text);
    }

    private static Integer toInteger(Object value) {
        if (value == null) return 1;
        if (value instanceof Integer) return (Integer) value;
        if (value instanceof Long) return ((Long) value).intValue();
        if (value instanceof Number) return ((Number) value).intValue();
        String text = String.valueOf(value).trim();
        if (text.isEmpty()) return 1;
        return Integer.parseInt(text);
    }

    private static BigDecimal toBigDecimal(Object value) {
        if (value == null) return BigDecimal.ZERO;
        if (value instanceof BigDecimal) return (BigDecimal) value;
        if (value instanceof Number) return BigDecimal.valueOf(((Number) value).doubleValue());
        String text = String.valueOf(value).trim();
        if (text.isEmpty()) return BigDecimal.ZERO;
        return new BigDecimal(text);
    }

    private static String toStringValue(Object value) {
        if (value == null) return null;
        if (value instanceof byte[]) {
            return new String((byte[]) value, java.nio.charset.StandardCharsets.UTF_8);
        }
        if (value instanceof char[]) {
            return new String((char[]) value);
        }
        return String.valueOf(value);
    }

    /**
     * 清空购物车
     */
    @Override
    public void clean() {
        Long userId = BaseContext.getCurrentId();
        shoppingCartMapper.deleteByUserId(userId);
    }

    /**
     * 删除购物车中的一个商品
     */
    @Override
    public void sub(ShoppingCartDTO shoppingCartDTO) {
        //TODO: 实现减少商品数量逻辑
    }
    
    /**
     * 从购物车删除商品（新接口）- 直接删除整个商品，不管数量多少
     * @param deleteFromCartDTO
     */
    @Override
    public void deleteFromCart(DeleteFromCartDTO deleteFromCartDTO) {
        Long userId = BaseContext.getCurrentId();
        Long id = deleteFromCartDTO.getId();
        Long productId = deleteFromCartDTO.getProductId();
        
        // 如果提供了ID，直接通过ID删除（适用于DIY商品）
        if (id != null) {
            log.info("删除购物车商品，用户ID: {}, 购物车项ID: {}", userId, id);
            shoppingCartMapper.deleteById(id);
            log.info("成功删除购物车商品，购物车项ID: {}", id);
            return;
        }
        
        // 否则通过productId删除（普通商品）
        log.info("删除购物车商品，用户ID: {}, 商品ID: {}", userId, productId);
        
        // 构造查询条件
        CartItem cartItemQuery = CartItem.builder()
                .userId(userId)
                .productId(productId)
                .build();
        
        // 查询购物车中是否已存在该商品
        List<CartItem> cartItems = shoppingCartMapper.list(cartItemQuery);
        
        if (cartItems != null && !cartItems.isEmpty()) {
            CartItem existingItem = cartItems.get(0);
            // 直接删除整条记录，不管数量多少
            shoppingCartMapper.deleteById(existingItem.getId());
            log.info("成功删除购物车商品，购物车项ID: {}", existingItem.getId());
        }
    }

    @Override
    public void updateDiy(UpdateDiyCartDTO updateDiyCartDTO) {
        Long userId = BaseContext.getCurrentId();
        Long id = updateDiyCartDTO.getId();
        String diyData = updateDiyCartDTO.getDiyData();
        if (id == null || diyData == null || diyData.trim().isEmpty()) {
            throw new BaseException("参数不完整");
        }
        CartItem existing = shoppingCartMapper.getById(id);
        if (existing == null || !userId.equals(existing.getUserId())) {
            throw new BaseException("购物车项不存在");
        }
        if (existing.getProductId() == null || existing.getProductId() >= 0) {
            throw new BaseException("仅支持更新DIY设计");
        }
        int rows = shoppingCartMapper.updateDiyData(id, userId, diyData);
        if (rows <= 0) {
            throw new BaseException("更新失败");
        }
        log.info("更新DIY购物车设计成功, cartId={}, userId={}", id, userId);
    }
}
