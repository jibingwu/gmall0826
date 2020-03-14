package com.atguigu.gmall.order.config;


import com.atguigu.core.utils.RsaUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.annotation.PostConstruct;
import java.security.PublicKey;

@Data
@Slf4j
@ConfigurationProperties("cart.jwt")
public class JwtProperties {
    private String pubKeyPath;//公钥
    private String cookieName; //cookiesName=名称
    private PublicKey publicKey; //公钥对象
    /**
     *  加载到这个类就执行
     *    @PostConstruct 在构造方方法之前执行这个方法
     */
    @PostConstruct
    public  void init(){
        try {
            //获取公钥
            this.publicKey = RsaUtils.getPublicKey(pubKeyPath);
        } catch (Exception e) {
            log.debug("获取公钥失败！，请检查的公钥是否配置成功！",e.getMessage());

        }

    }



}
