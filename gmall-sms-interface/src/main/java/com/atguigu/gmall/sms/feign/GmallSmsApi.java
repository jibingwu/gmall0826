package com.atguigu.gmall.sms.feign;

import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.sms.vo.ItemSaleVO;
import com.atguigu.gmall.sms.vo.SkuSaleVO;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

public interface GmallSmsApi {
    @PostMapping("/sms/skubounds/skusale/save")
    public Resp<Object> saveSkuSaleInfo(@RequestBody SkuSaleVO skuSaleVO);

    //通过skuId查询营销信息（5）
    @GetMapping("/sms/skubounds/{skuId}")
    public Resp<List<ItemSaleVO>> queryItemSaleBySkuId(@PathVariable("skuId") Long skuId);


}
