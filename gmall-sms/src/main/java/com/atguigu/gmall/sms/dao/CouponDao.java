package com.atguigu.gmall.sms.dao;

import com.atguigu.gmall.sms.entity.CouponEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 优惠券信息
 * 
 * @author jsonwu
 * @email jsonwu@atguigu.com
 * @date 2020-02-18 20:31:40
 */
@Mapper
public interface CouponDao extends BaseMapper<CouponEntity> {
	
}
