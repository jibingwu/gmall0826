package com.atguigu.gmall.wms;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.hystrix.EnableHystrix;
import org.springframework.cloud.openfeign.EnableFeignClients;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@EnableFeignClients
@SpringBootApplication
@EnableDiscoveryClient
@EnableSwagger2
@MapperScan("com.atguigu.gmall.wms.dao")
public class GmallWmsApplication {
    public static void main(String[] args) {
        SpringApplication.run(GmallWmsApplication.class, args);
    }

}
