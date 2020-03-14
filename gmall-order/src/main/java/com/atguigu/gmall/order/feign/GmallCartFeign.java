package com.atguigu.gmall.order.feign;

import com.atguigu.gamll.cart.api.GmallCartApi;
import com.atguigu.gmall.oms.api.GmallOmsApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient("cart-service")
public interface GmallCartFeign extends GmallCartApi
{
}
