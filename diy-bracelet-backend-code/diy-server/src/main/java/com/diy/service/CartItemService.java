package com.diy.service;

import com.diy.dto.AddToCartDTO;
import com.diy.dto.DeleteFromCartDTO;
import com.diy.dto.ShoppingCartDTO;
import com.diy.entity.CartItem;
import com.diy.vo.CartItemListVO;

import java.util.List;

public interface CartItemService {
    /**
     * 添加到购物车
     */
    void add(ShoppingCartDTO shoppingCartDTO);
    
    /**
     * 添加商品到购物车（新接口）
     * @param addToCartDTO
     */
    CartItem addToCart(AddToCartDTO addToCartDTO);

    /**
     * 查看购物车
     */
    List<CartItem> list();
    
    /**
     * 查看购物车（包含商品信息）
     */
    CartItemListVO listWithProductInfo();

    /**
     * 清空购物车
     */
    void clean();

    /**
     * 删除购物车中的一个商品
     */
    void sub(ShoppingCartDTO shoppingCartDTO);
    
    /**
     * 从购物车删除商品（新接口）
     * @param deleteFromCartDTO
     */
    void deleteFromCart(DeleteFromCartDTO deleteFromCartDTO);

    /**
     * 更新 DIY 购物车项的设计数据（重新设计后保存）
     */
    void updateDiy(com.diy.dto.UpdateDiyCartDTO updateDiyCartDTO);
}
