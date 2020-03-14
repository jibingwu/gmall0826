package com.atguigu.gmall.order.entity;

import com.atguigu.gmall.pms.entity.SkuSaleAttrValueEntity;
import com.atguigu.gmall.sms.vo.ItemSaleVO;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class Cart {
    private  Long skuId; //商品的id
    private  String  title;//商品的标题
    private  String  defaultImgae; //图片
    private Boolean check ;
    List<SkuSaleAttrValueEntity> saleAttrs; //销售属性
    private BigDecimal price; //商品价格
    private BigDecimal CurrentPrice; //商品价格
    private  BigDecimal count;//商品数量
    private  List<ItemSaleVO> sales; //营销属性

}
