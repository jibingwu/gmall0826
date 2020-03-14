package com.atguigu.gmall.oms.dao;

import com.atguigu.gmall.oms.entity.OrderItemEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单项信息
 * 
 * @author jsonwu
 * @email jsonwu@atguigu.com
 * @date 2020-03-10 13:46:11
 */
@Mapper
public interface OrderItemDao extends BaseMapper<OrderItemEntity> {
	
}
