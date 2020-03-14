package com.atguigu.gmall.order.controller;


import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.oms.vo.OrderSubmitVO;
import com.atguigu.gmall.order.service.OrderService;
import com.atguigu.gmall.order.vo.OrderConfirmVO;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author jsonwu
 */
@RestController
@RequestMapping("order")
public class OrderController {

      @Autowired
     private OrderService orderService;
    @ApiOperation("购物车-》结算页面")
    @GetMapping("confirm")
    public Resp<OrderConfirmVO> confirm() {
        OrderConfirmVO orderConfirmVO = this.orderService.confirm();
        return Resp.ok(orderConfirmVO);
    }


    @ApiOperation("结算页面到-成功提交到订单页面")
    @PostMapping("submit")
    public Resp<Object>  toSubmit(@RequestBody OrderSubmitVO orderItemVO){
        this.orderService.toSubmit(orderItemVO);
        return Resp.ok(null);

    }













}
