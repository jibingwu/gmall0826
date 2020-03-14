package com.atguigu.gmall.cart.config;

import com.atguigu.gmall.cart.interceptor.LoginInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 注册自定义拦截器
 * WebMvcConfigurer
 * @author
 */
@Configuration
public class MvcConfig  implements WebMvcConfigurer {

@Autowired
private LoginInterceptor loginInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
          registry.addInterceptor(loginInterceptor).addPathPatterns("/**");
    }
}
