package com.atguigu.gmall.wms.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.wms.vo.SkuLockVO;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.Query;
import com.atguigu.core.bean.QueryCondition;

import com.atguigu.gmall.wms.dao.WareSkuDao;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import com.atguigu.gmall.wms.service.WareSkuService;
import org.springframework.util.CollectionUtils;


@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private WareSkuDao wareSkuDao;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private static final String KEY_PREFIX = "wms:lock:";

    @Override
    public PageVo queryPage(QueryCondition params) {
        IPage<WareSkuEntity> page = this.page(
                new Query<WareSkuEntity>().getPage(params),
                new QueryWrapper<WareSkuEntity>()
        );

        return new PageVo(page);
    }


    @Override
    public List<SkuLockVO> checkAndLock(List<SkuLockVO> lockVOS) {

        //需要需要加锁的商品 是否为null
        if (CollectionUtils.isEmpty(lockVOS)) {
            return null;
        }
        //只要有商品，都先加锁
        lockVOS.forEach(lockVO -> {
            this.checkLock(lockVO);
        });

        //如果所有商品中只要有一条记录锁定失败，就要解锁
        List<SkuLockVO> successLockVO = lockVOS.stream().filter(SkuLockVO::getLock).collect(Collectors.toList());
        List<SkuLockVO> errorLockVO = lockVOS.stream().filter(skuLockVO -> !skuLockVO.getLock()).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(errorLockVO)) {
            successLockVO.forEach(lockVO -> {
                this.wareSkuDao.unlockStock(lockVO.getWareSkuId(), lockVO.getCount());
            });
            return lockVOS;
        }

        //ba库存的锁定的信息保存到redis中，方便将来解锁库存
        String orderToken = lockVOS.get(0).getOrderToken(); //todo
        //把锁定好的商品，保存到redi中去。方便边将来解锁库存。下单前一步失败后
        this.stringRedisTemplate.opsForValue().set(KEY_PREFIX + orderToken, JSON.toJSONString(lockVOS));
        return null; //如果都锁定成功返回null


    }

    private void checkLock(SkuLockVO skuLockVO) {
        //使用公平锁，进行加锁
        RLock fairLock = this.redissonClient.getFairLock("lock:" + skuLockVO.getSkuId());
        //加锁
        fairLock.lock();
        //验库存查询库存是否有货
        List<WareSkuEntity> wareSkuEntities = this.wareSkuDao.checkStock(skuLockVO.getSkuId(), skuLockVO.getCount());
        //判断库存是否有货
        if (CollectionUtils.isEmpty(wareSkuEntities)) {
            //库存不足锁定失败
            skuLockVO.setLock(false);
            fairLock.unlock();
            return;
        }
        //验库
        if (this.wareSkuDao.lockStock(wareSkuEntities.get(0).getId(), skuLockVO.getCount()) == 1) {
            //加锁成功
            skuLockVO.setLock(true);
            //记住那个id被锁住了
            skuLockVO.setWareSkuId(wareSkuEntities.get(0).getId());
        } else {
            //加锁失败。
            skuLockVO.setLock(false);
        }
        //释放锁
        fairLock.unlock();

    }


}