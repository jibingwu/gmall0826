package com.atguigu.gmall.oms.vo;

import com.atguigu.gmall.pms.entity.SkuSaleAttrValueEntity;
import com.atguigu.gmall.sms.vo.ItemSaleVO;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 去结算商品详情vo
 */

@Data
public class OrderItemVO {
    private  Long skuId; //商品的id
    private  String  title;//商品的标题
    private  String  imgae; //图片
    //private Boolean check ;
    List<SkuSaleAttrValueEntity> saleAttrs; //销售属性
    private BigDecimal price; //商品价格
    private  BigDecimal count;//商品数量
    private BigDecimal weight;  //商品的重量
    private  List<ItemSaleVO> sales; //营销属性
    private boolean  store; //是否有货


}
