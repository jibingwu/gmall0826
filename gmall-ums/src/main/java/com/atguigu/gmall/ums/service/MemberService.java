package com.atguigu.gmall.ums.service;

import com.atguigu.gmall.ums.entity.MemberEntity;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.QueryCondition;


/**
 * 会员
 *
 * @author jsonwu
 * @email jsonwu@atguigu.com
 * @date 2020-03-05 21:41:42
 */
public interface MemberService extends IService<MemberEntity> {

    PageVo queryPage(QueryCondition params);

    Boolean checkData(String data, Integer type);

    void register( MemberEntity memberEntity,String code);


    MemberEntity queryUser(String username, String password);
}

