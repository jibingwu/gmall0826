package com.atguigu.gmall.oms.service;

import com.atguigu.gmall.oms.vo.OrderSubmitVO;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.gmall.oms.entity.OrderEntity;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.QueryCondition;


/**
 * 订单
 *
 * @author jsonwu
 * @email jsonwu@atguigu.com
 * @date 2020-03-10 13:46:11
 */
public interface OrderService extends IService<OrderEntity> {

    PageVo queryPage(QueryCondition params);

    OrderEntity saveOrder(OrderSubmitVO submitVO);
}

