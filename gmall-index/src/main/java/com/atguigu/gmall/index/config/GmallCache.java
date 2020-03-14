package com.atguigu.gmall.index.config;

import org.springframework.core.annotation.AliasFor;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.lang.annotation.*;

/*
 手写aop
 @Transactional 模仿事物注解手写aop解决业务代码代码冗余，
   提取缓存、并发带来事物安全。
 */

@Target({ElementType.METHOD}) //目标 标注可以用在哪上面 ElementType是个枚举
@Retention(RetentionPolicy.RUNTIME)  //指定在运行上 这个枚举类型的常量描述保留注释的各种策略
@Documented //解表明这个注释是由 javadoc记录的，在默认情况下也有类似的记录工具。 如果一个类型声明被注释了文档化，它的注释成为公共API的一部分
public  @interface GmallCache {

    /**
     * 设置缓存的前缀
     * 默认是：分钟
     * @return
     */
    String prefix() default "";

    /**
     * 缓存有效时间
     *  默认是：5分
     * @return
     */

     int  timeout() default 5 ;

    /**
     * redis缓存数据数据同时过期。
     * 防止雪崩设置时间的随机的范围
     * 默认是:5分钟
     *
     * @return
     */
    int  random() default  5;

    /**
     * 防止缓存击穿，加锁 ，穿透缓存为null数据保存到redis数据中
     *
     * @return
     */
    String  lock() default "lock";





}
