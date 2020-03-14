package com.atguigu.gmall.wms.listener;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.wms.dao.WareSkuDao;
import com.atguigu.gmall.wms.vo.SkuLockVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class StockListener {

   @Autowired
   private StringRedisTemplate stringRedisTemplate;

   @Autowired
   private WareSkuDao wareSkuDao;


    private static final String KEY_PREFIX = "wms:lock:";

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "WMS-UNLOCK-QUEUE", durable = "true"),
            exchange = @Exchange(value = "ORDER-EXCHANGE", ignoreDeclarationExceptions = "ture", type = ExchangeTypes.TOPIC),
            key = {"wms.unlock"}
    ))
    public  void  unlockListener(String OrderToken){

        String  lockJson = this.stringRedisTemplate.opsForValue().get(KEY_PREFIX + OrderToken);
         if (StringUtils.isEmpty(lockJson)){
             return;
         }
         //把lockJson反序列化
        List<SkuLockVO> skuLockVOS = JSON.parseArray(lockJson, SkuLockVO.class);
        skuLockVOS.forEach(skuLockVO -> {
            this.wareSkuDao.unlockStock(skuLockVO.getWareSkuId(),skuLockVO.getCount());

        });




    }


}
