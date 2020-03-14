package com.atguigu.gmall.order.vo;


import com.atguigu.gmall.oms.vo.OrderItemVO;
import com.atguigu.gmall.ums.entity.MemberReceiveAddressEntity;
import lombok.Data;

import java.util.List;

/**
 * @author jsonwu
 *
 * 去结算页面响应对象
 *
 */

@Data
public class OrderConfirmVO {

    //收货地址
    private List<MemberReceiveAddressEntity>  address;
    //商品详情（送货清单）
    private  List<OrderItemVO> items;
    //积分
    private Integer bounds ;
    //订单结算页面，防止订单重复刷新
    private  String orderToken;






}
