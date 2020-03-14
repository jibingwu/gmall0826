package com.atguigu.gmall.oms.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.oms.dao.OrderItemDao;
import com.atguigu.gmall.oms.entity.OrderItemEntity;
import com.atguigu.gmall.oms.feign.GmallPmsFeign;
import com.atguigu.gmall.oms.feign.GmallUmsFeign;
import com.atguigu.gmall.oms.vo.OrderItemVO;
import com.atguigu.gmall.oms.vo.OrderSubmitVO;

import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.ums.entity.MemberEntity;
import com.atguigu.gmall.ums.entity.MemberReceiveAddressEntity;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Date;
import java.util.List;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.Query;
import com.atguigu.core.bean.QueryCondition;

import com.atguigu.gmall.oms.dao.OrderDao;
import com.atguigu.gmall.oms.entity.OrderEntity;
import com.atguigu.gmall.oms.service.OrderService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;


@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {

    @Autowired
    private GmallUmsFeign umsFeign;
    @Autowired
    private GmallPmsFeign pmsFeign;
    @Autowired
    private OrderItemDao orderItemDao;


    @Override
    public PageVo queryPage(QueryCondition params) {
        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>()
        );

        return new PageVo(page);
    }


    @Transactional
    @Override
    public OrderEntity saveOrder(OrderSubmitVO submitVO) {
        //保存订单
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setMemberId(submitVO.getUserId());
        orderEntity.setOrderSn(submitVO.getOrderToken());
        orderEntity.setCreateTime(new Date());
        //需要用户信息 通过用户skuId
        Resp<MemberEntity> memberEntityResp = this.umsFeign.queryMemberById(submitVO.getUserId());
        MemberEntity memberEntity = memberEntityResp.getData();
        orderEntity.setMemberUsername(memberEntity.getUsername());
        orderEntity.setTotalAmount(submitVO.getTotalPrice());
        orderEntity.setPayType(submitVO.getPayType());
        orderEntity.setSourceType(0);
        orderEntity.setStatus(0);// 未付款
        orderEntity.setDeliveryCompany(submitVO.getDeliveryCompany());
        //积分需要调用

        //地址
        MemberReceiveAddressEntity address = submitVO.getAddress();
        orderEntity.setReceiverRegion(address.getRegion());
        orderEntity.setReceiverProvince(address.getProvince());
        orderEntity.setReceiverPostCode(address.getPostCode());
        orderEntity.setReceiverPhone(address.getPhone());
        orderEntity.setReceiverDetailAddress(address.getDetailAddress());
        orderEntity.setReceiverCity(address.getCity());
        orderEntity.setUseIntegration(submitVO.getBounds());
        orderEntity.setDeleteStatus(0);
        //保存
        this.save(orderEntity);

        //保存订单详情
        List<OrderItemVO> items = submitVO.getItems();
        System.out.println("items = " + items);
        if (!CollectionUtils.isEmpty(items)) {
            items.forEach(item -> {
                OrderItemEntity itemEntity = new OrderItemEntity();
                itemEntity.setOrderId(orderEntity.getId());
                itemEntity.setOrderSn(submitVO.getOrderToken());
                //sku信息
                Resp<SkuInfoEntity> skuInfoEntityResp = this.pmsFeign.querysSkuById(item.getSkuId());
                SkuInfoEntity skuInfoEntity = skuInfoEntityResp.getData();
                itemEntity.setSkuId(item.getSkuId());
                itemEntity.setSkuPrice(skuInfoEntity.getPrice());
                itemEntity.setSkuPic(skuInfoEntity.getSkuDefaultImg());
                itemEntity.setSkuName(skuInfoEntity.getSkuName());
                //销售属性
                Resp<List<SkuSaleAttrValueEntity>> listResp = this.pmsFeign.querySaleAttrBySpuId(item.getSkuId());
                List<SkuSaleAttrValueEntity> skuSaleAttrValueEntities = listResp.getData();
                itemEntity.setSkuAttrsVals(JSON.toJSONString(skuSaleAttrValueEntities));
                //查询spu信息
                Resp<SpuInfoEntity> spuInfoEntityResp = this.pmsFeign.querySpuById(skuInfoEntity.getSpuId());
                SpuInfoEntity spuInfoEntity = spuInfoEntityResp.getData();
                itemEntity.setSpuId(skuInfoEntity.getSpuId());
                itemEntity.setSpuName(spuInfoEntity.getSpuName());
                itemEntity.setCategoryId(spuInfoEntity.getCatalogId());
                //查询spu的描述信息
                Resp<SpuInfoDescEntity> spuInfoDescEntityResp = this.pmsFeign.querySkuDescBySpuId(spuInfoEntity.getId());
                SpuInfoDescEntity spuInfoDescEntity = spuInfoDescEntityResp.getData();
                itemEntity.setSkuPic(spuInfoDescEntity.getDecript());
                //查询品牌
                Resp<BrandEntity> brandEntityResp = this.pmsFeign.queryBrandsById(skuInfoEntity.getBrandId());
                BrandEntity brandEntity = brandEntityResp.getData();
                itemEntity.setSpuBrand(brandEntity.getName());

                System.out.println("brandEntity = " +itemEntity );

                this.orderItemDao.insert(itemEntity);
            });
        }

        //todo
        int i=1/0;
        System.out.println(i);
        return  orderEntity;
    }

}