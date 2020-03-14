package com.atguigu.gamll.cart.api;

import com.atguigu.core.bean.Resp;
import com.atguigu.gamll.cart.entity.Cart;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

public interface GmallCartApi {
    //查询购物车的选中列表
    @GetMapping("cart/check/{userId}")
    public Resp<List<Cart>> queryCarts(@PathVariable("userId") Long userId);



}
