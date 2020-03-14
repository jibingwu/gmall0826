package com.atguigu.gmall.order.interceptor;


import com.atguigu.core.utils.CookieUtils;
import com.atguigu.core.utils.JwtUtils;
import com.atguigu.gmall.order.config.JwtProperties;
import com.atguigu.gmall.order.entity.UserInfo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.UUID;

/**
 * 拦截器定义
 */
@EnableConfigurationProperties({JwtProperties.class})
@Component
public class LoginInterceptor extends HandlerInterceptorAdapter {

    private static final ThreadLocal<UserInfo> THREAD_LOCAL = new ThreadLocal<>();

    @Autowired
    private JwtProperties jwtProperties;


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //1.获取登入token
        String token = CookieUtils.getCookieValue(request, this.jwtProperties.getCookieName());
        //2、如果token不为null解析
        Long userId = null;
        if (StringUtils.isNotBlank(token)) {
            Map<String, Object> map = JwtUtils.getInfoFromToken(token, this.jwtProperties.getPublicKey());
            if (!CollectionUtils.isEmpty(map)) {
                userId = new Long(map.get("userId").toString());
            }
        }
        UserInfo userInfo = new UserInfo();    //3、获取userId(用户登入的id)
        userInfo.setUserId(userId);
        THREAD_LOCAL.set(userInfo);  //4、把得到的信息存放在THREAD_LOCAL
        return true;
    }


    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        THREAD_LOCAL.remove();
    }


    public static UserInfo getUserInfo() {
        return THREAD_LOCAL.get();
    }


}
