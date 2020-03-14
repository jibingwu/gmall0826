package com.atguigu.gmall.wms.vo;

import lombok.Data;

@Data
public class SkuLockVO {
    private  Long skuId;  //商品Id
    private  Integer count;// 商品数量
    private  Boolean lock; //锁的状态。是否锁，是否解锁了
    private Long wareSkuId; //锁定成功仓库的id
    private String orderToken; // 方便以订单为单位缓存订单的锁定信息

}
