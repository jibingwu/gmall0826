package com.atguigu.gmall.index.service;

import com.atguigu.gmall.pms.vo.CategoryVO;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface IndexService {
    public List<CategoryEntity> queryLvel1Category() ;

    List<CategoryVO> queryLv2WithSubByPid(Long pid);

    void testLock();
}
