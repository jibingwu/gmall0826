package com.atguigu.gmall.ums.dao;

import com.atguigu.gmall.ums.entity.MemberEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会员
 * 
 * @author jsonwu
 * @email jsonwu@atguigu.com
 * @date 2020-03-05 21:41:42
 */
@Mapper
public interface MemberDao extends BaseMapper<MemberEntity> {
	
}
