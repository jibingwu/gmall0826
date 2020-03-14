package com.atguigu.gmall.wms.dao;

import com.atguigu.gmall.wms.entity.WareSkuEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 商品库存
 * 
 * @author jsonwu
 * @email jsonwu@atguigu.com
 * @date 2020-02-22 00:16:07
 */
@Mapper
public interface WareSkuDao extends BaseMapper<WareSkuEntity> {

    // 验库存方法
    List<WareSkuEntity> checkStock(@Param("skuId") Long skuId, @Param("count") Integer count);

    // 锁库存
    int lockStock(@Param("id") Long id, @Param("count") Integer count);

    // 解库存
    int unlockStock(@Param("id") Long id, @Param("count") Integer count);
}
