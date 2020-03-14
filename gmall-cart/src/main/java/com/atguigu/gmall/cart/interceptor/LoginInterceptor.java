package com.atguigu.gmall.cart.interceptor;

import com.atguigu.core.utils.CookieUtils;
import com.atguigu.core.utils.JwtUtils;
import com.atguigu.gmall.cart.config.JwtProperties;
import com.atguigu.gmall.cart.entity.UserInfo;
import io.jsonwebtoken.Jwt;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.swing.table.TableRowSorter;
import java.security.interfaces.RSAPrivateCrtKey;
import java.util.Map;
import java.util.UUID;

/**
 * 拦截器定义
 */
@EnableConfigurationProperties({JwtProperties.class})
@Component
public class LoginInterceptor extends HandlerInterceptorAdapter {

    private  static final ThreadLocal<UserInfo> THREAD_LOCAL=new ThreadLocal<>();

    @Autowired
    private JwtProperties jwtProperties;


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //1.获取登入token
        String token = CookieUtils.getCookieValue(request, this.jwtProperties.getCookieName());
        //游客useKey(TOKEN)
        String userKey = CookieUtils.getCookieValue(request, this.jwtProperties.getUserKeyName());

        //2.如果都为null，需要设置
        if (StringUtils.isBlank(token) && StringUtils.isBlank(userKey)) {
            //就设置token
            userKey = UUID.randomUUID().toString().substring(0, 8);
            //设置userKey(tk)到cookie中
            Cookie cookie = new Cookie(this.jwtProperties.getUserKeyName(), userKey);
            cookie.setHttpOnly(true);
            cookie.setPath("/");
            cookie.setDomain("localhost");
            cookie.setMaxAge(this.jwtProperties.getExpireTiem());
            response.addCookie(cookie);

           // CookieUtils.setCookie(request, response, this.jwtProperties.getUserKeyName(), userKey, this.jwtProperties.getExpireTiem());

        }

        //3、如果token不为null解析
        Long userId=null;
        if (StringUtils.isNotBlank(token)) {
            Map<String, Object> map = JwtUtils.getInfoFromToken(token, this.jwtProperties.getPublicKey());
            if (!CollectionUtils.isEmpty(map)) {
              userId = new Long(map.get("userId").toString());
            }
        }
        //4、不管userKey为不空都要设置
        UserInfo userInfo=new UserInfo();
         userInfo.setUserId(userId);
         userInfo.setUserKey(userKey);
         //5、把得到的信息存放在THREAD_LOCAL

        THREAD_LOCAL.set(userInfo);


        return true;
    }


    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        THREAD_LOCAL.remove();
    }


    public static UserInfo getUserInfo(){
        return THREAD_LOCAL.get();
    }


}
