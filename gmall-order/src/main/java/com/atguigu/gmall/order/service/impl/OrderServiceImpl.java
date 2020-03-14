package com.atguigu.gmall.order.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.core.bean.Resp;
import com.atguigu.core.exception.OrderExeption;
import com.atguigu.gamll.cart.entity.Cart;
import com.atguigu.gmall.oms.entity.OrderEntity;
import com.atguigu.gmall.oms.vo.OrderItemVO;
import com.atguigu.gmall.oms.vo.OrderSubmitVO;
import com.atguigu.gmall.order.entity.UserInfo;
import com.atguigu.gmall.order.feign.*;
import com.atguigu.gmall.order.interceptor.LoginInterceptor;
import com.atguigu.gmall.order.service.OrderService;
import com.atguigu.gmall.order.vo.OrderConfirmVO;
import com.atguigu.gmall.pms.entity.SkuInfoEntity;
import com.atguigu.gmall.pms.entity.SkuSaleAttrValueEntity;;
import com.atguigu.gmall.sms.vo.ItemSaleVO;

import com.atguigu.gmall.ums.entity.MemberEntity;
import com.atguigu.gmall.ums.entity.MemberReceiveAddressEntity;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import com.atguigu.gmall.wms.vo.SkuLockVO;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private GmallWmsFeign gmallWmsFeign;
    @Autowired
    private GmallPmsFeign gmallPmsFeign;
    @Autowired
    private GmallSmsFeign gmallSmsFeign;
    @Autowired
    private GmallUmsFeign gmallUmsFeign;
    @Autowired
    private GmallCartFeign gmallCartFeign;
    @Autowired
    private GmallOmsFeign gmallOmsFeign;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    private static final String ORDERKEY_PREFIX = "order:token:";
    //private static final String CART_PREFIX = "cart:uid:"; //用户key的前缀
    @Autowired
    private ThreadPoolExecutor threadPoolExecutor;
    @Autowired
    private AmqpTemplate amqpTemplate;



    /**
     * 查询需要从购物车到结算页面的数据
     *
     * @return
     */

    @Override
    public OrderConfirmVO confirm() {
         OrderConfirmVO orderConfirmVO=new OrderConfirmVO();
        //1、先获取用户登入的信息。只有用户登入了，才有趣结算功能
        UserInfo userInfo = LoginInterceptor.getUserInfo();
        Long userId = userInfo.getUserId();
        if (userId == null) { //记住如果判断一个null。自定义异常
            throw new OrderExeption("用户登入已经过期");
        }
        //2、订单详细的查询
        CompletableFuture<Void> cartFuture =   CompletableFuture.supplyAsync(() -> {
            // 远程调用，查询购物车好选中的状态所以商品
            Resp<List<Cart>> listResp = this.gmallCartFeign.queryCarts(userId);
            List<Cart> carts = listResp.getData();
            return carts;
        },threadPoolExecutor).thenAcceptAsync(carts -> {
            //2.2 获取销售信息items
            List<OrderItemVO> items = carts.stream().map(cart -> {
                //创建商品详细VO
                OrderItemVO orderItemVO = new OrderItemVO();
                CompletableFuture<SkuInfoEntity> skuFuture = CompletableFuture.supplyAsync(() -> {
                    //通过通过获取到的商品
                    Resp<SkuInfoEntity> skuInfoEntityResp = this.gmallPmsFeign.querysSkuById(cart.getSkuId());
                    SkuInfoEntity skuInfoEntity = skuInfoEntityResp.getData();
                    if (skuInfoEntity != null) {
                        orderItemVO.setSkuId(skuInfoEntity.getSkuId());
                        orderItemVO.setTitle(skuInfoEntity.getSkuTitle());
                        orderItemVO.setImgae(skuInfoEntity.getSkuDefaultImg());
                        orderItemVO.setPrice(skuInfoEntity.getPrice());
                        orderItemVO.setWeight(skuInfoEntity.getWeight());
                        orderItemVO.setCount(cart.getCount());
                    }
                    return skuInfoEntity;
                },threadPoolExecutor);
                //查询sku销售信息,cart的 的skuId查询
                CompletableFuture<Void> saleAttrFuture = skuFuture.thenAcceptAsync(skuInfoEntity -> {
                    if (skuInfoEntity != null) {
                        Resp<List<SkuSaleAttrValueEntity>> listResp = this.gmallPmsFeign.querySkuBySkuId(cart.getSkuId());
                        List<SkuSaleAttrValueEntity> skuSaleAttrValueEntitys = listResp.getData();
                        orderItemVO.setSaleAttrs(skuSaleAttrValueEntitys);
                    }
                },threadPoolExecutor);
                //查询营销信息通过
                // private  List<ItemSaleVO> sales; //营销属性

                CompletableFuture<Void> itemSaleVOFuture = skuFuture.thenAcceptAsync(skuInfoEntity -> {

                    if (skuInfoEntity != null) {
                        Resp<List<ItemSaleVO>> listResp = this.gmallSmsFeign.queryItemSaleBySkuId(cart.getSkuId());
                        List<ItemSaleVO> itemSaleVO = listResp.getData();
                        orderItemVO.setSales(itemSaleVO);
                    }

                },threadPoolExecutor);
                //是否有货
                CompletableFuture<Void> stockFuture = skuFuture.thenAcceptAsync(skuInfoEntity -> {

                    if (skuInfoEntity != null) {
                        Resp<List<WareSkuEntity>> listResp = this.gmallWmsFeign.queryWareSkusBySkuId(cart.getSkuId());

                        List<WareSkuEntity> wareSkuEntity = listResp.getData();
                        if (!CollectionUtils.isEmpty(wareSkuEntity)) {
                            boolean flag = wareSkuEntity.stream().anyMatch(ware -> ware.getStock() > 0);
                            orderItemVO.setStore(flag);
                        }
                    }
                },threadPoolExecutor);
                 //执行
                CompletableFuture.allOf(saleAttrFuture,itemSaleVOFuture,stockFuture).join();
                return orderItemVO;

            }).collect(Collectors.toList());
               orderConfirmVO.setItems(items);
        },threadPoolExecutor);


        //2、获取收货地址
        CompletableFuture<Void> meAddressFuture = CompletableFuture.runAsync(() -> {
            Resp<List<MemberReceiveAddressEntity>> listResp = this.gmallUmsFeign.queryAddressesByUserId(userId);
            List<MemberReceiveAddressEntity> memberReceiveAddressEntity = listResp.getData();

            orderConfirmVO.setAddress(memberReceiveAddressEntity);

        },threadPoolExecutor);

        //3、获取到积分
        CompletableFuture<Void>   memberFuture = CompletableFuture.runAsync(() -> {
            Resp<MemberEntity> memberEntityResp = this.gmallUmsFeign.queryMemberById(userId);
            MemberEntity memberEntity = memberEntityResp.getData();
            if (memberEntity != null) {
                orderConfirmVO.setBounds(memberEntity.getIntegration());
            }

        },threadPoolExecutor);
        //4、防止重复防止重复提交 生成唯一的token
        //把提交的token保存到redis。如果
        CompletableFuture<Void> tokenFuture = CompletableFuture.runAsync(() -> {
            String orderToken = IdWorker.getTimeId();
            orderConfirmVO.setOrderToken(orderToken);
            this.stringRedisTemplate.opsForValue().set(ORDERKEY_PREFIX + orderToken, orderToken, 3, TimeUnit.HOURS);
        },threadPoolExecutor);

        CompletableFuture.allOf(cartFuture,meAddressFuture,memberFuture,tokenFuture).join();
        return orderConfirmVO;
    }


     //提交
    @Override
    public void toSubmit(OrderSubmitVO submitVO) {
        //1、校验是否重复提交、原子性
        String orderToken = submitVO.getOrderToken();
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] " +
                "then return redis.call('del', KEYS[1]) " +
                "else return 0 end";
        Boolean flag = this.stringRedisTemplate
                .execute(new DefaultRedisScript<>(script, Boolean.class), Arrays.asList(ORDERKEY_PREFIX + orderToken), orderToken);
        System.out.println("flag = " + flag);
           //获取到的redis中的orderToken和我提交的一样
           if(!flag){
               throw  new OrderExeption("您多次提交，请重新提交");
           }
            //2、校验商品的价格
        BigDecimal totalPrice = submitVO.getTotalPrice(); //商品总价
        
        List<OrderItemVO> items = submitVO.getItems();//获取确认页面商品详情
          if(CollectionUtils.isEmpty(items)){
              throw  new OrderExeption("您还没选中商品，请选中需要购买的商品！");
          }
          //遍历商品的订单详情，获取数据库的价格，计算实时价格
        BigDecimal currentPrice = items.stream().map(item -> {
            //远程调用查询skuInfo
            Resp<SkuInfoEntity> skuInfoEntityResp = this.gmallPmsFeign.querysSkuById(item.getSkuId());
            SkuInfoEntity skuInfoEntity = skuInfoEntityResp.getData();
            if (skuInfoEntity != null) {
                //计算价格所有商品的的总价格
                return skuInfoEntity.getPrice().multiply(item.getCount());
            }
            return new BigDecimal(0);
        }).reduce((t1, t2) -> t1.add(t2)).get();
          //-1  0 1
        if (totalPrice.compareTo(currentPrice) !=0){
            throw new OrderExeption("页面已经过期，请刷新页面！");
        }

        //3、校验商品的库存并锁定库存
        List<SkuLockVO> skuLockVOS = items.stream().map(orderItemVO -> {
            SkuLockVO skuLockVO = new SkuLockVO();
            skuLockVO.setSkuId(orderItemVO.getSkuId());
            skuLockVO.setCount(orderItemVO.getCount().intValue());
            skuLockVO.setOrderToken(submitVO.getOrderToken());
            return skuLockVO;

        }).collect(Collectors.toList());
        Resp<List<SkuLockVO>> listResp = this.gmallWmsFeign.checkAndLock(skuLockVOS);
        List<SkuLockVO> skuLockVO = listResp.getData();
        if (!CollectionUtils.isEmpty(skuLockVO)){
            throw new OrderExeption("商品库存不足！"+ JSON.toJSONString(skuLockVO));
        }
         //4下单操作远程调用
         UserInfo userInfo = LoginInterceptor.getUserInfo();
         Long userId = userInfo.getUserId();
         submitVO.setUserId(userId);
         //保存订单
        try {
            Resp<OrderEntity> orderEntityResp = this.gmallOmsFeign.saveOrder(submitVO);
        } catch (Exception e) {
            e.printStackTrace();
            //如果订单创建失败。立即释放库存 todo
            this.amqpTemplate.convertAndSend("ORDER-EXCHANGE","wms.unlock",orderToken);
        }

        //5、下单成功后，使用消息队列删除购物车对应的商品信息.（userId,skuIds）
        HashMap<Object, Object> map = new HashMap<>();
         map.put("userId",userId);
         // items是订单详情。可以通过订单详情获取每个skuId
        List<Long> skuIds = items.stream().map(OrderItemVO::getSkuId).collect(Collectors.toList());
        map.put("skuIds", JSON.toJSONString(skuIds) );

        this.amqpTemplate.convertAndSend("ORDER-EXCHANGE","cart.delete",map);


        //TODO 需要返回下单的信息,需要从保存订单获取需要用户支付多少内容。

    }
}
