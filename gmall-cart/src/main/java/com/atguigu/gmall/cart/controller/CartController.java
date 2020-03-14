package com.atguigu.gmall.cart.controller;

import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.cart.entity.Cart;
import com.atguigu.gmall.cart.entity.UserInfo;
import com.atguigu.gmall.cart.interceptor.LoginInterceptor;
import com.atguigu.gmall.cart.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("cart")
public class CartController {

    @Autowired
    private CartService cartService;

   @PostMapping
    public Resp<Object>  addCart(@RequestBody Cart cart){

        this.cartService.addCart(cart);
        return Resp.ok(null);
   }


    @GetMapping
    public Resp<List<Cart>>  queryCart(){
       List<Cart> carts=this.cartService.queryCart();
       return Resp.ok(carts);


    }


     //查询购物车的选中列表
    @GetMapping("check/{userId}")
    public Resp<List<Cart>>  queryCarts(@PathVariable("userId") Long userId){
        List<Cart> carts=this.cartService.queryCarts(userId);
        System.out.println("carts = " + carts);
        return Resp.ok(carts);
    }

    @GetMapping("{skuId}")
    public  Resp<Object>   updateNum(@PathVariable("skuId") Long skuId){

        this.cartService.deleteCart(skuId);
        return Resp.ok(null);

    }


    @GetMapping("update")
    public  Resp<Object>   updateNum(@RequestBody Cart cart){

       this.cartService.updateNum(cart);
       return Resp.ok(null);

    }

    @GetMapping("check")
    public  Resp<Object>   updateCheck(@RequestBody Cart cart){
        this.cartService.updateCheck(cart);
        return Resp.ok(null);
    }










    @GetMapping("test")
    public String  testInterceptor(){
        UserInfo userInfo = LoginInterceptor.getUserInfo();
        String toString = userInfo.toString();
        System.out.println("toString = " + toString);


        return "interceptor!";

    }



}
