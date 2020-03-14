package com.atguigu.gmall.pms.vo;

import com.atguigu.gmall.pms.entity.SpuInfoEntity;
import lombok.Data;

import java.util.List;
@Data
public class SpuInfoVO extends SpuInfoEntity {

    //保存图片描述
    private List<String> supImages;
    //pms_product_attr_value 保存psu商品的基本属性
    private List<ProductAttrValueVO>  baseAttrs;
    //保存sku信息，一个商品的spu有多个sku
    private List<SkuInfoVO> skus;





}
