package com.atguigu.gmall.pms.service;

import com.atguigu.gmall.pms.entity.AttrGroupEntity;
import com.atguigu.gmall.pms.vo.AttrGroupVO;
import com.atguigu.gmall.pms.vo.ItemGroupVO;
import com.baomidou.mybatisplus.extension.service.IService;

import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.QueryCondition;

import java.util.List;


/**
 * 属性分组
 *
 * @author jsonwu
 * @email jsonwu@atguigu.com
 * @date 2020-02-18 17:27:03
 */
public interface AttrGroupService extends IService<AttrGroupEntity> {

    PageVo queryPage(QueryCondition params);

    PageVo queryByCidPage(Long catid, QueryCondition condition);

    AttrGroupVO queryById(Long gid);

     //查询分类的下的分组及其参数
    List<AttrGroupVO> queryByCid(Long catId);

    List<ItemGroupVO> queryItemGroupsBySpuIdAndCid(Long spuId, Long cid);
}

