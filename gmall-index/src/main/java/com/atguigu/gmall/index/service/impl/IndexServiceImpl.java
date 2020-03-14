package com.atguigu.gmall.index.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.index.Feign.GmllPmsFeign;
import com.atguigu.gmall.index.config.GmallCache;
import com.atguigu.gmall.index.service.IndexService;
import com.atguigu.gmall.pms.vo.CategoryVO;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class IndexServiceImpl implements IndexService {

    @Autowired
    private GmllPmsFeign gmallPmsFeign;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    //key_prifix ：index功能 cates:哪个模块
    private final String KEY_PREFIX = "index:cates:";


    @Override
    public List<CategoryEntity> queryLvel1Category() {


        Resp<List<CategoryEntity>> listResp = this.gmallPmsFeign.queryCategory(1, null);
        return listResp.getData();
    }




    @Override
    @GmallCache(prefix = "index:cates:", timeout = 14400, random = 3600, lock = "lock")
    public List<CategoryVO> queryLv2WithSubByPid(Long pid) {
            //通过pi查询2级分类及其子分类
        Resp<List<CategoryVO>> listResp = this.gmallPmsFeign.queryCategoryWithSubByPid(pid);
        List<CategoryVO> categoryVOS = listResp.getData();
        return categoryVOS;
    }






    public List<CategoryVO> queryLv2WithSubByPid1(Long pid) {
        //先查询缓存
        String categoryJson = stringRedisTemplate.opsForValue().get(KEY_PREFIX + pid);
        //如果不等于null就反序列化出去
        if (StringUtils.isNotBlank(categoryJson)) {
            return JSON.parseArray(categoryJson, CategoryVO.class);
        }
        //缓存中没有数据，就查询数据库
        Resp<List<CategoryVO>> listResp = this.gmallPmsFeign.queryCategoryWithSubByPid(pid);
        List<CategoryVO> categoryVOS = listResp.getData();
        //判断返回的数据是否为null
        if (!CollectionUtils.isEmpty(categoryVOS)) {
            JSON.toJSONString(categoryVOS);
            this.stringRedisTemplate.opsForValue().set(KEY_PREFIX + pid, JSON.toJSONString(categoryVOS), 10, TimeUnit.DAYS);
        }
        return categoryVOS;
    }


    @Override
    public void testLock() {
        // 1. 从redis中获取锁,setnx
        //通过uuid防止误删
        String uuid = UUID.randomUUID().toString();
        Boolean lock = this.stringRedisTemplate.opsForValue().setIfAbsent("lock", uuid, 3, TimeUnit.SECONDS);
        System.out.println("lock = " + lock);

        if (lock) {
            //查询redis里面的 null
            String value = this.stringRedisTemplate.opsForValue().get("num");

            //没有该值就直接return
            if (StringUtils.isBlank(value)) {
                return;
            }
            //如果就值 就请就把 int
            int num = Integer.parseInt(value);
            //然后把 redis中的num+1
            this.stringRedisTemplate.opsForValue().set("num", String.valueOf(++num));


            // 2. 释放锁 del  利用lua脚本，防止誤刪
          //  String script = "if redis.call('get', KEYS[1]) == ARGV[1] " +
                //    "then return redis.call('del', KEYS[1]) " +
                   // "else return 0 end";
            //.stringRedisTemplate.execute(new DefaultRedisScript<>(script),Arrays.asList("lock"),uuid);


            //2. 判断获取到redis中的锁是否等于正在执行业务的锁，如果执行完成业务就释放锁
            if(StringUtils.equals(this.stringRedisTemplate.opsForValue().get("lock"),uuid)){
            //释放锁
              this.stringRedisTemplate.delete("lock");
              }
        } else {
            //每隔1秒重试获取锁
            try {
                Thread.sleep(500);
                testLock();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }


    }

}
