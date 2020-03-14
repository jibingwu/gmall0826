package com.atguigu.gmall.cart.service.Impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.cart.entity.Cart;
import com.atguigu.gmall.cart.entity.UserInfo;
import com.atguigu.gmall.cart.feign.GmallPmsFeign;
import com.atguigu.gmall.cart.feign.GmallSmsFeign;
import com.atguigu.gmall.cart.interceptor.LoginInterceptor;
import com.atguigu.gmall.cart.service.CartService;
import com.atguigu.gmall.pms.entity.SkuInfoEntity;
import com.atguigu.gmall.pms.entity.SkuSaleAttrValueEntity;
import com.atguigu.gmall.sms.vo.ItemSaleVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;


import java.math.BigDecimal;
import java.security.Key;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author jswu
 */

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private GmallPmsFeign gmallPmsFeign;

    @Autowired
    private GmallSmsFeign gmallSmsFeign;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String CART_PREFIX = "cart:uid:"; //用户key的前缀

    private static final String PRICE_PREFIX = "cart:price:";//及时更新价格前缀


    @Override
    public void addCart(Cart cart) {
        String key = this.generatedKeyStatus();
        //获取用户操作购物对象，也就是从redis设置对象
        BoundHashOperations<String, Object, Object> hashOps = this.redisTemplate.boundHashOps(key);
        //获取添加的商品的skuId
        String skuId = cart.getSkuId().toString();
        //获取添加的商品的数量
        BigDecimal count = cart.getCount();
        //判断购物车有没有商品，也就是通过hashOps的key来查询（里面的Map的key） Map<String,Map<String,String>>
        if (hashOps.hasKey(skuId)) {
            //有，就更新数量。
            String cartJson = hashOps.get(skuId).toString();
            //把获取到的redis中的jsoncart数据序列化成 Cart对象
            cart = JSON.parseObject(cartJson, Cart.class);
            //通过解析获取对象，更新数量:查询+新增
            cart.setCount(cart.getCount().add(count));
        } else {
            //没有，就添加。
            Resp<SkuInfoEntity> skuInfoEntityResp = this.gmallPmsFeign.querysSkuById(cart.getSkuId());
            SkuInfoEntity skuInfoEntity = skuInfoEntityResp.getData();
            cart.setTitle(skuInfoEntity.getSkuTitle());
            cart.setDefaultImgae(skuInfoEntity.getSkuDefaultImg());
            cart.setCheck(true);
            cart.setPrice(skuInfoEntity.getPrice());
            //营销属性
            Resp<List<ItemSaleVO>> itemSaleResp = this.gmallSmsFeign.queryItemSaleBySkuId(cart.getSkuId());
            List<ItemSaleVO> itemSaleVOS = itemSaleResp.getData();
            cart.setSales(itemSaleVOS);
            //销售属性
            Resp<List<SkuSaleAttrValueEntity>> listResp = this.gmallPmsFeign.querySkuBySkuId(cart.getSkuId());
            List<SkuSaleAttrValueEntity> skuSaleAttrValueEntitys = listResp.getData();
            cart.setSaleAttrs(skuSaleAttrValueEntitys);
            //设置当前价格(及时更新价格)
            this.redisTemplate.opsForValue().set(PRICE_PREFIX + skuId, skuInfoEntity.getPrice().toString());
        }
        //最后把商品添加到购物车
        hashOps.put(skuId, JSON.toJSONString(cart));
    }


    @Override
    public List<Cart> queryCart() {

        //获取登入信息
        UserInfo userInfo = LoginInterceptor.getUserInfo();
        //1.以userKey作为key查询未登录的购物车信息
        String unLoginKey = CART_PREFIX + userInfo.getUserKey();
        BoundHashOperations<String, Object, Object> unLoginHashOps = this.redisTemplate.boundHashOps(unLoginKey);
        List<Object> cartJsons = unLoginHashOps.values();
        List<Cart> unLoginCartJsons = null;
        if (!CollectionUtils.isEmpty(cartJsons)) {
            unLoginCartJsons = cartJsons.stream().map(cartJson -> {
                Cart cart = JSON.parseObject(cartJson.toString(), Cart.class);
                //从缓存中获取到最新的价格
                //cart数据的skuId
                String cuPrice = this.redisTemplate.opsForValue().get(PRICE_PREFIX + cart.getSkuId());
                cart.setCurrentPrice(new BigDecimal(cuPrice));
                return cart;

            }).collect(Collectors.toList());
        }
        //2.查询登入状态。
        Long userId = userInfo.getUserId();
        if (userId == null) {
            return unLoginCartJsons;
        }

        //3.如果已经登入了。。。遍历未登录的合并到登入的购物车去
        //通过用户的 CART_PREFIX+userId去获取，也就是用户登入的id
        String loginKey = CART_PREFIX + userId;
        BoundHashOperations<String, Object, Object> loginHashOps = this.redisTemplate.boundHashOps(loginKey);
        if (!CollectionUtils.isEmpty(unLoginCartJsons)) {
            unLoginCartJsons.forEach(cart -> {
                //判断未登录的购物车在已登入的购物车是否存在
                if (loginHashOps.hasKey(cart.getSkuId().toString())) {
                    //存在更新数量
                    //遍历未登录的购物车，获取cart的 skuId。通过cart的skuId获取到登入的购物车里面和是否一样
                    String cartJson = loginHashOps.get(cart.getSkuId().toString()).toString();
                    //把获取到缓存数据。转成cart
                    cart = JSON.parseObject(cartJson, Cart.class);
                    BigDecimal count = cart.getCount();
                    //增加数量
                    cart.setCount(cart.getCount().add(count));
                }
                //
                loginHashOps.put(cart.getSkuId().toString(), JSON.toJSONString(cart));
            });
            //
        }
        //未登录的购物车合并到已经登入的购物车，需要删除未登录的购物车的缓存信息
        this.redisTemplate.delete(unLoginKey);

        //4、查询登入购物车
        List<Object> loginCartJsons = loginHashOps.values();
        if (!CollectionUtils.isEmpty(loginCartJsons)) {
            return loginCartJsons.stream().map(cartJson -> {
                Cart cart = JSON.parseObject(cartJson.toString(), Cart.class);
                //获取及时更新价格
                cart.setCurrentPrice(new BigDecimal(this.redisTemplate.opsForValue().get(PRICE_PREFIX + cart.getSkuId())));
                return cart;
            }).collect(Collectors.toList());
        }

        return null;
    }

    @Override
    public void updateNum(Cart cart) {
        String key = this.generatedKeyStatus();
        BoundHashOperations<String, Object, Object> hashOps = this.redisTemplate
                .boundHashOps(key);
        String cartJson = hashOps.get(cart.getSkuId().toString()).toString();
        if (StringUtils.isNotBlank(cartJson)) {
            BigDecimal count = cart.getCount();
            cart = JSON.parseObject(cartJson, Cart.class);
            cart.setCount(count);
            hashOps.put(cart.getSkuId().toString(), JSON.toJSONString(cart));
        }

    }

    @Override
    public void updateCheck(Cart cart) {
        String key = this.generatedKeyStatus();
        BoundHashOperations<String, Object, Object> hashOps = this.redisTemplate.boundHashOps(key);
        String cartJson = hashOps.get(cart.getSkuId().toString()).toString();
        if (StringUtils.isNotBlank(cartJson)) {
            Boolean check = cart.getCheck();
            cart = JSON.parseObject(cartJson, Cart.class);
            cart.setCheck(check);
            //修改后。一定要修改再一次设置到redis
            hashOps.put(cart.getSkuId().toString(), JSON.toJSONString(cart));
        }
    }

    @Override
    public void deleteCart(Long skuId) {
        String key = this.generatedKeyStatus();
        this.redisTemplate.delete(key + skuId);
    }




    /**
     * 提取，获取登入状态
     *
     * @return
     */
    private String generatedKeyStatus() {
        //获取用户登入信息，判断用户的状态是否登入
        UserInfo userInfo = LoginInterceptor.getUserInfo();
        //判断登入状态,组装key
        String key = CART_PREFIX;
        if (userInfo.getUserId() == null) {
            key += userInfo.getUserKey();
        } else {
            key += userInfo.getUserId();
        }
        return key;
    }



    /**
     * 通过userid去查询
     * @param userId
     * @return
     */
    @Override
    public List<Cart> queryCarts(Long userId) {
        String  key =CART_PREFIX+userId;

        BoundHashOperations<String, Object, Object> hashOps = this.redisTemplate.boundHashOps(key);
        //hashOps获取值
        List<Object> cartJsons = hashOps.values();
        if(CollectionUtils.isEmpty(cartJsons)){
            return  null;
        }
        return cartJsons.stream().map(cartJson ->
                JSON.parseObject(cartJson.toString(), Cart.class)).filter(cart -> cart.getCheck()).collect(Collectors.toList());

    }


}
