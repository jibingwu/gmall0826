package com.atguigu.gmall.index.controller;


import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.index.service.IndexService;
import com.atguigu.gmall.pms.vo.CategoryVO;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;



@RestController
@RequestMapping("index")
public class IndexController {
 @Autowired
  private IndexService indexService;
     //Request URL: http://localhost:2000/api/index/cates




    @GetMapping("testlock")
    public Resp<Object> testLock(){
        this.indexService.testLock();

        return Resp.ok(null);
    }

    @GetMapping("cates")
     public Resp<List<CategoryEntity>> queryLvel1Category(){
        List<CategoryEntity>  categoryLev= this.indexService.queryLvel1Category();
        return  Resp.ok(categoryLev);
    }


    @GetMapping("cates/{pid}")
    public  Resp<List<CategoryVO>>   queryLv2WithSubByPid(@PathVariable("pid") Long pid){
        List<CategoryVO>  categoryVOS= this.indexService.queryLv2WithSubByPid(pid);
        return  Resp.ok(categoryVOS);

    }

}
