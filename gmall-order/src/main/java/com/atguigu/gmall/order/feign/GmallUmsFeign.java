package com.atguigu.gmall.order.feign;

import com.atguigu.gmall.oms.api.GmallOmsApi;
import com.atguigu.gmall.ums.api.GmallUmsApi;
import com.atguigu.gmall.wms.api.GmallWmsApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient("ums-service")
public interface GmallUmsFeign extends GmallUmsApi
{

}
