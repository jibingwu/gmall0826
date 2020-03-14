package com.atguigu.gmall.pms.vo;

import com.atguigu.gmall.pms.entity.SkuInfoEntity;
import com.atguigu.gmall.pms.entity.SkuSaleAttrValueEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * SkuInfoVO 封装sku
 */

@Data
public class SkuInfoVO extends SkuInfoEntity {

    private List<String> images;

    private List<SkuSaleAttrValueEntity> saleAttrs;

    // 积分优惠信息
    private BigDecimal growBounds;
    private BigDecimal buyBounds;
    /**
     * 优惠生效情况[1111（四个状态位，从右到左）;
     * 0 - 无优惠，成长积分是否赠送;
     * 1 - 无优惠，购物积分是否赠送;
     * 2 - 有优惠，成长积分是否赠送;
     * 3 - 有优惠，购物积分是否赠送【状态位0：不赠送，1：赠送】]
     */

    private List<Integer> work;

    // 打折信息
    private Integer fullCount;
    private BigDecimal discount;
    private Integer ladderAddOther;

    // 满减信息
    private BigDecimal fullPrice;
    private BigDecimal reducePrice;
    private Integer fullAddOther;


}
