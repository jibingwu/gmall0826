package com.atguigu.gmall.auth.controller;

import com.atguigu.core.bean.Resp;
import com.atguigu.core.utils.CookieUtils;
import com.atguigu.core.utils.JwtUtils;
import com.atguigu.gmall.auth.config.JwtProperties;
import com.atguigu.gmall.auth.service.AuthService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

@EnableConfigurationProperties({JwtProperties.class})
@RestController
@RequestMapping("auth")
public class AuthController {
    @Autowired
    private AuthService authService;
    @Autowired
    private JwtProperties jwtProperties;

    @PostMapping("accredit")
    public Resp<Object> accreditAction(@RequestParam("username") String userName, @RequestParam("password") String password, HttpServletRequest request, HttpServletResponse response) throws InterruptedException {

        //1.获取到token
        String token = this.authService.accreditAction(userName, password);
        System.out.println("token = " + token);

        //4.设置给cookies
       // CookieUtils.setCookie(request, response, this.jwtProperties.getCookieName(), token, this.jwtProperties.getExpire() * 60);

        Cookie cookie = new Cookie(this.jwtProperties.getCookieName(), token);
                cookie.setHttpOnly(true);
                cookie.setPath("/");
                cookie.setMaxAge(this.jwtProperties.getExpire() * 60);//最大有效时间
                cookie.setDomain("localhost");
                response.addCookie(cookie);

        String cookieValue = CookieUtils.getCookieValue(request,this.jwtProperties.getCookieName());
        System.out.println("cookieValue = " + cookieValue);


   /*     Cookie[] cookies = request.getCookies();
        for (Cookie cookie : cookies) {
            System.out.println("cookie.getValue() = " + cookie.getValue());

        }
*/

        return Resp.ok(null);
    }

}
