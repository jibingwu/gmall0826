package com.atguigu.gmall.pms.service;

import com.atguigu.gmall.pms.vo.CategoryVO;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.QueryCondition;

import java.util.List;


/**
 * 商品三级分类
 *
 * @author jsonwu
 * @email jsonwu@atguigu.com
 * @date 2020-02-18 17:27:02
 */
public interface CategoryService extends IService<CategoryEntity> {

    PageVo queryPage(QueryCondition params);

    List<CategoryEntity> queryCategory(Integer level, Long parentCid);

    List<CategoryVO> queryCategoryWithSubByPid(Long pid);
}

