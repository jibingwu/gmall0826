package com.atguigu.gmall.search.service;

import com.atguigu.gmall.search.vo.SearchParamVO;
import com.atguigu.gmall.search.vo.SearchResponseVO;
import org.springframework.stereotype.Repository;

@Repository
public interface SearchServices {
    SearchResponseVO search(SearchParamVO searchParamVO);
}
