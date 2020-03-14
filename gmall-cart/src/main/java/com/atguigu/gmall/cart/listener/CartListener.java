package com.atguigu.gmall.cart.listener;


import com.alibaba.fastjson.JSON;
import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.cart.feign.GmallPmsFeign;
import com.atguigu.gmall.pms.entity.SkuInfoEntity;
import org.apache.commons.lang.StringUtils;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.List;

@Component
public class CartListener {

    @Autowired
    private GmallPmsFeign gmallPmsFeign;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private static final String PRICE_PREFIX = "cart:price:";//及时更新价格前缀
    private static final String CART_PREFIX = "cart:uid:"; //用户key的前缀

    @RabbitListener(bindings = @QueueBinding( //PMS-SPU-EXCHANGE
            value = @Queue(value = "CART-PRICE-QUEUE", durable = "true"),
            exchange = @Exchange(value = "PMS-SPU-EXCHANGE", ignoreDeclarationExceptions = "ture", type = ExchangeTypes.TOPIC),
            key = {"item.update"} //item.update
    ))

    public void listener(Long spuId) {
        Resp<List<SkuInfoEntity>> listResp = this.gmallPmsFeign.querySkusBySpuId(spuId);
        List<SkuInfoEntity> skuInfoEntities = listResp.getData();
        if (CollectionUtils.isEmpty(skuInfoEntities)) {
            return;
        }
        skuInfoEntities.forEach(skuInfoEntity -> this.stringRedisTemplate.opsForValue().set(PRICE_PREFIX + skuInfoEntity.getSkuId(),
                        skuInfoEntity.getPrice().toString()
                ));


    }

    @RabbitListener(bindings = @QueueBinding(
                    value=@Queue(value = "ORDER-CART-QUEUE",durable = "true"),
                     exchange = @Exchange(value = "ORDER-EXCHANGE",ignoreDeclarationExceptions="true",type = ExchangeTypes.TOPIC),
            key = {"cart.delete"}
    ))
    public  void  deleteCart(HashMap<Object, Object> map){
         //判断是否为null
        if (CollectionUtils.isEmpty(map)){
            return ;
        }
        Long userId = (Long)map.get("userId");
        String skuIdsString = map.get("skuIds").toString();
        System.out.println("skuIdsString = " + skuIdsString);

        if (StringUtils.isEmpty(skuIdsString)){
             return;
        }
        //存取到map集合中的数据取出来，因为存在集合中的存取的是以list+json存取的 一个list集合 取出来的时候 JSON.parseArray
        List<String> skuIds = JSON.parseArray(skuIdsString, String.class);
        BoundHashOperations<String, Object, Object> hashOps = this.stringRedisTemplate.boundHashOps(CART_PREFIX + userId);
        //删除的时候是通过多个删除。所以 toArray
        hashOps.delete(skuIds.toArray());



    }


}
