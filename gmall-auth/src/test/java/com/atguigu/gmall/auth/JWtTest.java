package com.atguigu.gmall.auth;

import com.atguigu.core.utils.JwtUtils;
import com.atguigu.core.utils.RsaUtils;
import org.junit.Before;
import org.junit.Test;


import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.Map;

public class JWtTest {
    private static final String pubKeyPath = "F:\\\\0826allProject\\\\Ecommerce\\gmall-0826\\\\secret\\\\rsa.pub";

    private static final String priKeyPath = "F:\\\\0826allProject\\\\Ecommerce\\gmall-0826\\\\secret\\\\rsa.pri";

    private PublicKey publicKey;

    private PrivateKey privateKey;

    @Test
    public void testRsa() throws Exception {
        RsaUtils.generateKey(pubKeyPath, priKeyPath, "234");
    }

    @Before
    public void testGetRsa() throws Exception {
        this.publicKey = RsaUtils.getPublicKey(pubKeyPath);
        this.privateKey = RsaUtils.getPrivateKey(priKeyPath);
    }

    @Test
    public void testGenerateToken() throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("id", "11");
        map.put("username", "liuyan");
        // 生成token
        String token = JwtUtils.generateToken(map, privateKey, 5);
        System.out.println("token = " + token);
    }

    @Test
    public void testParseToken() throws Exception {
        String token = "eyJhbGciOiJSUzI1NiJ9.eyJ1c2VyTmFtZSI6InpzNjY2NjY2IiwidXNlcklkIjo2LCJleHAiOjE1ODM2MTIyODN9.VtFgsnDl0x8uNNwwfy2J79ptqNxvn-7UPvZneJsO-n3LvFpQ7RfPXFWqlKxGYilTkIgGhkBWFNbJV-NXtTBugbQyAO__j8mz-qi_ytieUukrF4xBeLDkbjzF2D0EnmtZLXwVn8nAIVSfwQuHBghbtUofh-561iesUYx_w39bTKcB1t2BQLCug-A4k5UCE-35qKtllmFKmcZXo-As9BR8fV7payAXHuv4ZitbbKTY5RDPldaYtbjsB_x_Gl9vnUn9swTk6cNtN9sOz7Ps3TPZ0UAsZ6G62rQQW4NXtw9bSR1yMHE6SaGCxjpxlrLOm9xxIsilwMLLSG02xJLN5e296A";
        // 解析token
        Map<String, Object> map = JwtUtils.getInfoFromToken(token, publicKey);
        System.out.println("id: " + map.get("userId")); //id
        System.out.println("userName: " + map.get("userName"));//userName
    }

}
