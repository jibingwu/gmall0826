package com.atguigu.gmall.auth.config;


import com.atguigu.core.utils.RsaUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.annotation.PostConstruct;
import java.io.File;
import java.security.PrivateKey;
import java.security.PublicKey;

@Data
@Slf4j
@ConfigurationProperties(prefix = "auth.jwt")
public class JwtProperties {

    private String priKeyPath; //私钥
    private String pubKeyPath;//公钥

    private String secret;//秘钥
    private Integer expire;//30 #过期时间30分钟
    private String cookieName; //cookiesName=名称

    private PublicKey publicKey; //公钥对象
    private PrivateKey privateKey;//私钥对象     PrivateKey

    /**
     *  加载到这个类就执行
     *    @PostConstruct 在构造方方法之前执行这个方法
     */
    @PostConstruct
    public  void init(){
        try {
            //判断文件是否存在
            File  priKey = new File(priKeyPath);
            File  pubKey = new File(priKeyPath);
            //判断公钥是否存在
            if(!priKey.exists()||!pubKey.exists()){
                //不存在，生成公钥或者私钥
                RsaUtils.generateKey(pubKeyPath,priKeyPath,secret);

            }
            //如果有值就获取
            this.publicKey= RsaUtils.getPublicKey(pubKeyPath);
            this.privateKey=RsaUtils.getPrivateKey(priKeyPath);
        } catch (Exception e) {
            e.printStackTrace();
        }


    }




}
