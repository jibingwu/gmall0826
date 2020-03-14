package com.atguigu.gmall.pms.dao;

import com.atguigu.gmall.pms.entity.AttrEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品属性
 * 
 * @author jsonwu
 * @email jsonwu@atguigu.com
 * @date 2020-02-18 17:27:03
 */

@Mapper
public interface AttrDao extends BaseMapper<AttrEntity> {
	
}
