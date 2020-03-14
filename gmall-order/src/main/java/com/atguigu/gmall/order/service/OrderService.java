package com.atguigu.gmall.order.service;

import com.atguigu.gmall.oms.vo.OrderSubmitVO;
import com.atguigu.gmall.order.vo.OrderConfirmVO;

public interface OrderService {

    OrderConfirmVO confirm();

    void toSubmit(OrderSubmitVO orderItemVO);
}
