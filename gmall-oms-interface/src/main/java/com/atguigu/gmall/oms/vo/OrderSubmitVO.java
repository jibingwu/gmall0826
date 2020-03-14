package com.atguigu.gmall.oms.vo;

import com.atguigu.gmall.ums.entity.MemberReceiveAddressEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 提交订单响应vo
 */

/**
 * @author jsonwu
 */
@Data
public class OrderSubmitVO {
    private  String orderToken; //防止重复提交
    private BigDecimal totalPrice; //总价格，校验价格变化
    private MemberReceiveAddressEntity  address; //收货人信息
    private Integer PayType; //支付方式
    private String deliveryCompany;//配送方式
    private List<OrderItemVO> items; // 订单详细
    private Integer bounds;// 积分信息
     private Long userId;









}
