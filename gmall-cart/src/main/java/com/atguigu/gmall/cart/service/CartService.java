package com.atguigu.gmall.cart.service;

import com.atguigu.gmall.cart.entity.Cart;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface CartService {

    void addCart(Cart cart);

    List<Cart> queryCart();

    void updateNum(Cart cart);

    void updateCheck(Cart cart);

    void deleteCart(Long skuId);

    List<Cart> queryCarts(Long userId);
}
