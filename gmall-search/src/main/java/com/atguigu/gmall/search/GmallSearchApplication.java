package com.atguigu.gmall.search;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableDiscoveryClient //注册中心
@EnableFeignClients //远程调用
@SpringBootApplication
public class GmallSearchApplication {

    public static void main(String[] args) {

        SpringApplication.run(GmallSearchApplication.class, args);
    }

}
