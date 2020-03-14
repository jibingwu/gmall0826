package com.atguigu.gmall.index.Feign;

import com.atguigu.gmall.pms.api.GmallPmsApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient("pms-service")
public  interface GmllPmsFeign  extends GmallPmsApi {


}
