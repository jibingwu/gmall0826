package com.atguigu.gmall.ums.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

/**
 * security配置
 *
 * @author  json
 */
@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        //授权所有用户访问
        http.authorizeRequests().antMatchers("/**").permitAll();
        //禁用掉csrf
        http.csrf().disable();
    }
}
