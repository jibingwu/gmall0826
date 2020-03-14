package com.atguigu.gmall.sms.service;

import com.atguigu.gmall.sms.vo.ItemSaleVO;
import com.atguigu.gmall.sms.vo.SkuSaleVO;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.gmall.sms.entity.SkuBoundsEntity;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.QueryCondition;

import java.util.List;


/**
 * 商品sku积分设置
 *
 * @author jsonwu
 * @email jsonwu@atguigu.com
 * @date 2020-02-18 20:31:39
 */
public interface SkuBoundsService extends IService<SkuBoundsEntity> {

    PageVo queryPage(QueryCondition params);

    void saveSkuSaleInfo(SkuSaleVO skuSaleVO);

    
    List<ItemSaleVO> queryItemSaleBySkuId(Long skuId);
}

