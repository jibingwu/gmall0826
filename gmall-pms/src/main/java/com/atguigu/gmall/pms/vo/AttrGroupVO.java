package com.atguigu.gmall.pms.vo;

import com.atguigu.gmall.pms.entity.AttrAttrgroupRelationEntity;
import com.atguigu.gmall.pms.entity.AttrEntity;
import com.atguigu.gmall.pms.entity.AttrGroupEntity;
import lombok.Data;

import java.util.List;

/***
 * 继承分组实体
 * 封装 属性组vo
 *
 */
@Data
public class AttrGroupVO extends AttrGroupEntity {
    //AttrEntity  属性
    private List<AttrEntity> attrEntities;
    //AttrAttrgroupRelationEntity  分组和属性的中间表
    private List<AttrAttrgroupRelationEntity> relations;


}
