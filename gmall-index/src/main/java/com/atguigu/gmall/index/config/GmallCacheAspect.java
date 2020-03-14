package com.atguigu.gmall.index.config;


import com.alibaba.fastjson.JSON;
import org.apache.commons.lang.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

import org.aspectj.lang.reflect.MethodSignature;
import org.aspectj.weaver.ast.And;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * 切面类,完成注解Gmallcache注解功能的
 */

@Component
@Aspect
public class GmallCacheAspect {
    //注入redis
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
     //注入redisson
    @Autowired
    private RedissonClient redissonclient;
    /**
     * 定义环绕通知
     * @return
     * @throws Throwable
     *
     *
     *  @GmallCache(prefix = "index:cates:", timeout = 14400, random = 3600, lock = "lock")
     *      List<CategoryVO> queryLv2WithSubByPid(Long pid)
     */
    @Around("@annotation(com.atguigu.gmall.index.config.GmallCache)")
    public  Object  around(ProceedingJoinPoint  JoinPoint) throws Throwable{
          //获取方法签
        MethodSignature signature = (MethodSignature)JoinPoint.getSignature();
          System.out.println("signature = " + signature);
          //获取方法的对象
        Method method = signature.getMethod();
         //获取方法的指定的注解对象
        GmallCache annotation = method.getAnnotation(GmallCache.class);
        System.out.println("annotation = " + annotation);
        //通过注解对象获取到前缀 还有一些参数
        String prefix = annotation.prefix();
        //获取参数方法的参数
         Object[] args = JoinPoint.getArgs();
         String param = Arrays.asList(args).toString();
         //获取方法的返回值类型
        Class<?> returnType = method.getReturnType();
        System.out.println("returnType(返回的对象) = " + returnType);
        // 拦截前代码块：
             //先拦截获取缓存中的数据有没有，先判断,不为null就转为把 jsondata数据转换为对象返回
             // opsForValue（）.get()得到的是一个json数据
             //通过fastjson JSON.parseObject（）。把一个json转换为转为为对象
        String jsonData= this.stringRedisTemplate.opsForValue().get(prefix + param);
         if(StringUtils.isNotBlank(jsonData)){
             return JSON.parseObject(jsonData,returnType);
         }
         
          //
          //如果没有数据，就给上加锁,加锁是为了防止缓存击穿
           //通过注解对象获取到参数lock
        String lock = annotation.lock();
        RLock rlock = this.redissonclient.getLock(lock + param);
        //加锁
        rlock.lock();

         //第二次 判断缓存中有没有，有直接返回(加锁的过程中，别的请求可能已经把数据放入缓存)
        String jsonData1= this.stringRedisTemplate.opsForValue().get(prefix + param);
        if(StringUtils.isNotBlank(jsonData1)){
            return JSON.parseObject(jsonData1,returnType);
        }
         //执行目标方法
        Object result = JoinPoint.proceed(JoinPoint.getArgs());
        //拦截代码后：放入缓存 释放锁
         //缓存过期时间
        int timeout = annotation.timeout();
        //防止雪崩设置时间的随机的范围
        int random = annotation.random();
        //第一个参数：key，第二参数是存储的数据，第一个
        this.stringRedisTemplate
                .opsForValue()
                .set(prefix+param,JSON.toJSONString(result),timeout+new Random().nextInt(random), TimeUnit.MINUTES);
        //释放锁
        rlock.unlock();
        return result;


    }


}
