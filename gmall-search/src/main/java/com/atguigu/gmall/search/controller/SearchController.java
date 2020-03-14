package com.atguigu.gmall.search.controller;

import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.search.service.SearchServices;
import com.atguigu.gmall.search.vo.SearchParamVO;
import com.atguigu.gmall.search.vo.SearchResponseAttrVO;
import com.atguigu.gmall.search.vo.SearchResponseVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("search")
public class SearchController {

    @Autowired
    private SearchServices searchService;

    @GetMapping
    public Resp<SearchResponseVO> search(SearchParamVO searchParamVO){

        // 查询业务方法
        SearchResponseVO searchResponseVO =  this.searchService.search(searchParamVO);
       // ，返回查询结果，给前端
        return  Resp.ok(searchResponseVO);


    }


}
