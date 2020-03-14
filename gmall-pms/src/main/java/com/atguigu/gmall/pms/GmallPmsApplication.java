package com.atguigu.gmall.pms;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.openfeign.EnableFeignClients;
import springfox.documentation.swagger2.annotations.EnableSwagger2;


@EnableDiscoveryClient //开启注册nacos中心
@RefreshScope //开启nacos文件配置自动更新
@EnableFeignClients
@EnableSwagger2 //开启swagger2注解
@MapperScan("com.atguigu.gmall.pms.dao") //开启dao接口扫描
@SpringBootApplication
public class GmallPmsApplication {
    public static void main(String[] args)    {
        SpringApplication.run(GmallPmsApplication.class, args);
    }

}
