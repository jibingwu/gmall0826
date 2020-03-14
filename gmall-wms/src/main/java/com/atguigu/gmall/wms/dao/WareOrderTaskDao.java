package com.atguigu.gmall.wms.dao;

import com.atguigu.gmall.wms.entity.WareOrderTaskEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 库存工作单
 * 
 * @author jsonwu
 * @email jsonwu@atguigu.com
 * @date 2020-02-22 00:16:07
 */
@Mapper
public interface WareOrderTaskDao extends BaseMapper<WareOrderTaskEntity> {
	
}
