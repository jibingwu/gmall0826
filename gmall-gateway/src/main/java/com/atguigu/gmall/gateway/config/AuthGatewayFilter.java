package com.atguigu.gmall.gateway.config;

import com.atguigu.core.utils.JwtUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;


/**
 * 自定义gateWay过滤器，过滤需要验证的服务
 */

@Component
@EnableConfigurationProperties({JwtProperties.class})
public class AuthGatewayFilter implements GatewayFilter {

    @Autowired
    private JwtProperties jwtProperties;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        //1.
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();
        //2、获取所有cookies
        MultiValueMap<String, HttpCookie> cookies = request.getCookies();
        System.out.println("cookies"+cookies);
        if (CollectionUtils.isEmpty(cookies) || !cookies.containsKey(this.jwtProperties.getCookieName())) {
            //响应状态码,无权限
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.setComplete();
        }

        //如果是有，获取cookie
        HttpCookie cookie = cookies.getFirst(this.jwtProperties.getCookieName());
        if (cookie == null || StringUtils.isEmpty(cookie.getValue())) {
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.setComplete();
        }

        try {
            //校验cookie是否登入那个 ,第一参数获取cookie的值.
            JwtUtils.getInfoFromToken(cookie.getValue(), this.jwtProperties.getPublicKey());
            //校验成功放行。
            return chain.filter(exchange);
        } catch (Exception e) {
            e.printStackTrace();
        }
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        return response.setComplete();
    }
}

