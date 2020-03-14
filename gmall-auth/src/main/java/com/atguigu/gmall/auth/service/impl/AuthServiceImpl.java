package com.atguigu.gmall.auth.service.impl;

import com.atguigu.core.bean.Resp;
import com.atguigu.core.exception.MemberExeption;
import com.atguigu.core.utils.JwtUtils;
import com.atguigu.gmall.auth.config.JwtProperties;
import com.atguigu.gmall.auth.service.AuthService;
import com.atguigu.gmall.ums.api.GmallUmsApi;
import com.atguigu.gmall.ums.entity.MemberEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;
import sun.rmi.runtime.Log;

import java.util.HashMap;
@Slf4j
@EnableConfigurationProperties({JwtProperties.class})
@Service
public class AuthServiceImpl implements AuthService {

  @Autowired
  private GmallUmsApi gmallUmsApil;

  @Autowired
  private JwtProperties jwtProperties;  //获取自己配置的jwt属性

    @Override
    public String accreditAction(String username, String password) {

        try {
            //1.先根据用户和密码名查询
            Resp<MemberEntity> memberEntityResp = this.gmallUmsApil.queryUser(username, password);
            MemberEntity memberEntity = memberEntityResp.getData();
            //2.如果查询没有，就返回null
            if(memberEntity==null){
                throw new MemberExeption("用户、密码输入错误！");
            }
            //3.如果有，生成token
            HashMap<String, Object> map = new HashMap<>();
            map.put("userId",memberEntity.getId());
            map.put("userName",memberEntity.getUsername());

            String  token = JwtUtils.generateToken(map, this.jwtProperties.getPrivateKey(), this.jwtProperties.getExpire());
            //4 把token设置给 cookies
            return token;
        } catch (Exception e) {
            log.debug("生成token信息失败{}"+e);
            e.printStackTrace();
        }
        //如果没有返回null

        return  null;
    }
}
