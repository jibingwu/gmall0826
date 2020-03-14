package com.atguigu.gmall.pms.service.impl;

import com.atguigu.gmall.pms.dao.SkuInfoDao;
import com.atguigu.gmall.pms.entity.SkuInfoEntity;
import com.sun.org.apache.bcel.internal.generic.NEW;
import io.seata.common.util.CollectionUtils;
import org.apache.catalina.webresources.TomcatJarInputStream;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.Query;
import com.atguigu.core.bean.QueryCondition;

import com.atguigu.gmall.pms.dao.SkuSaleAttrValueDao;
import com.atguigu.gmall.pms.entity.SkuSaleAttrValueEntity;
import com.atguigu.gmall.pms.service.SkuSaleAttrValueService;


@Service("skuSaleAttrValueService")
public class SkuSaleAttrValueServiceImpl extends ServiceImpl<SkuSaleAttrValueDao, SkuSaleAttrValueEntity> implements SkuSaleAttrValueService {

    @Autowired
    private SkuInfoDao skuInfoDao;

    @Override
    public PageVo queryPage(QueryCondition params) {
        IPage<SkuSaleAttrValueEntity> page = this.page(
                new Query<SkuSaleAttrValueEntity>().getPage(params),
                new QueryWrapper<SkuSaleAttrValueEntity>()
        );

        return new PageVo(page);
    }


    @Override
    public List<SkuSaleAttrValueEntity> querySaleAttrBySpuId(Long spuId) {
        //先根据spuId查询所有的sku
        List<SkuInfoEntity> skuInfoEntities = this.skuInfoDao.selectList(new QueryWrapper<SkuInfoEntity>().eq("spu_id", spuId));
        //查询的skuInfo为null，直接return
        if (CollectionUtils.isEmpty(skuInfoEntities)) {
            return null;

        }

    // 通过skuInfo 获取到所有skus 
        List<Long> skuIds = skuInfoEntities.stream().map(SkuInfoEntity::getSkuId).collect(Collectors.toList());

        //通过skus获取到查询出销售属性  pms_sku_sale_attr_value
        final List<SkuSaleAttrValueEntity>  skuSaleAttrValueEntities = this.list(new QueryWrapper<SkuSaleAttrValueEntity>().in("sku_id", skuIds));

        return skuSaleAttrValueEntities;
    }

}