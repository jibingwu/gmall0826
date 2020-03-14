package com.atguigu.gmall.item.vo;


import com.atguigu.gmall.pms.entity.SkuImagesEntity;
import com.atguigu.gmall.pms.entity.SkuSaleAttrValueEntity;
import com.atguigu.gmall.pms.vo.ItemGroupVO;
import com.atguigu.gmall.sms.vo.ItemSaleVO;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 商品详情总页面的数据模型
 */
@Data
public class ItemVO {
    //分类
    private Long categoryId;
    private String categoryName;

    //品牌
    private Long brandId;
    private String brandName;
    //spu信息
    private Long spuId;
    private String spuName;
    //sku信息
    private Long skuId;
    private String skuTitle;
    private String skuSubTtile;
    private BigDecimal price;
    private BigDecimal weight;


    private List<ItemSaleVO> sales; // 营销信息

    private Boolean store = false; //是否有货

    //需要sku所属的spu下的所有的suk信息  spu-> sku 一个spu又多个sku
    private List<SkuSaleAttrValueEntity> saleAttrs;

    private List<SkuImagesEntity> images; //sku的图片信息pms_spu_info_desc
    private List<String> desc;// 详细信息
    private List<ItemGroupVO> groups; //分组及组下的规格参数。


}
