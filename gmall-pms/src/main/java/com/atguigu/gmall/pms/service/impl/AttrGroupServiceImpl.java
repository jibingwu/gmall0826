package com.atguigu.gmall.pms.service.impl;


import com.atguigu.gmall.pms.dao.AttrAttrgroupRelationDao;
import com.atguigu.gmall.pms.dao.AttrDao;
import com.atguigu.gmall.pms.dao.ProductAttrValueDao;
import com.atguigu.gmall.pms.entity.AttrAttrgroupRelationEntity;
import com.atguigu.gmall.pms.entity.AttrEntity;
import com.atguigu.gmall.pms.entity.ProductAttrValueEntity;
import com.atguigu.gmall.pms.vo.AttrGroupVO;
import com.atguigu.gmall.pms.vo.ItemGroupVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.Query;
import com.atguigu.core.bean.QueryCondition;

import com.atguigu.gmall.pms.dao.AttrGroupDao;
import com.atguigu.gmall.pms.entity.AttrGroupEntity;
import com.atguigu.gmall.pms.service.AttrGroupService;
import org.springframework.util.CollectionUtils;


@Service("attrGroupService")
public class AttrGroupServiceImpl extends ServiceImpl<AttrGroupDao, AttrGroupEntity> implements AttrGroupService {

    @Autowired
    private AttrGroupDao attrGroupDao;
    @Autowired
    private AttrDao attrDao;
    @Autowired
    private AttrAttrgroupRelationDao attrAttrgroupRelationDao;
    @Autowired
    private ProductAttrValueDao productAttrValueDao;

    @Override
    public PageVo queryPage(QueryCondition params) {
        IPage<AttrGroupEntity> page = this.page(
                new Query<AttrGroupEntity>().getPage(params),
                new QueryWrapper<AttrGroupEntity>()
        );

        return new PageVo(page);
    }

    /**
     * 查询三级分类的业务方法
     *
     * @param catid
     * @param condition
     * @return
     */
    @Override
    public PageVo queryByCidPage(Long catid, QueryCondition condition) {
        /**
         * 业务逻辑说明
         * IPage 自己封装的Ipage对象 （以前是这样 QueryWrapper<AddressEntity> queryWrapper = new QueryWrapper();）
         * 第一个参数 是查询分页的条件，第二参数通过分类id查询
         */

        IPage<AttrGroupEntity> pageVo = this.page(
                new Query<AttrGroupEntity>().getPage(condition),
                new QueryWrapper<AttrGroupEntity>().eq("catelog_id", catid));
        //返回的是自己封装的PageVo工具 把pageVo拷贝给PageVo
        return new PageVo(pageVo);
    }

    /**
     * 查询分组的下面的规格参数
     * 设计到了 attrAttrgroupRelationDao
     * arrtDao、attrGroup
     *
     * @param gid
     * @return
     */


    @Override
    public AttrGroupVO queryById(Long gid) {
        // 创建自定义的分组。
        AttrGroupVO attrGroupVO = new AttrGroupVO();
        //先查询出分组 通过分组attr_group_id （gid）查询出分组
        AttrGroupEntity attrGroupEntity = this.attrGroupDao.selectById(gid);
        //然后把查询到的结果拷贝给AttrGroup
        BeanUtils.copyProperties(attrGroupEntity, attrGroupVO);

        //查询分组下的关联关系
        List<AttrAttrgroupRelationEntity> relations = this.attrAttrgroupRelationDao.selectList(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_group_id", gid));
        //判断关联关系是否为空,如果为空，直接返回

        if (CollectionUtils.isEmpty(relations)) {
            return attrGroupVO;

        }
        attrGroupVO.setRelations(relations);

        //收集分组下的所有规格id  分组表和属性表 有个中间表关联的
        //查询中间获得规格表（属性）的attrId
        List<Long> attrIds = relations.stream().map(relation ->
                relation.getAttrId()


        ).collect(Collectors.toList()
        );

        //通过中间得到属性表的后，查询出关联的数据
        //查询分组下所有规格（属性）的参数,批量查询
        List<AttrEntity> attrEntities = attrDao.selectBatchIds(attrIds);
        //attrGroupVO 然后把查询的结果设置给AttrGroupVO
        attrGroupVO.setAttrEntities(attrEntities);


        return attrGroupVO;
    }

    //查询分类下的分组及其规格参数
    @Override
    public List<AttrGroupVO> queryByCid(Long catId) {
        //通过分类id查询所有分组
        List<AttrGroupEntity> attrGroupEntities = this.list(new QueryWrapper<AttrGroupEntity>().eq("catelog_id", catId));
        //在通过分组查出分组下的每一组的规格参数
        //利用stream流的 map方法把一个list集合转换成
        List<AttrGroupVO> attrGroupVOs = attrGroupEntities.stream().map(attrGroupEntity -> {

            return this.queryById(attrGroupEntity.getAttrGroupId());

        }).collect(Collectors.toList());

        return attrGroupVOs;
    }


    @Override
    public List<ItemGroupVO> queryItemGroupsBySpuIdAndCid(Long spuId, Long cid) {
        //先根据cid查询分组
        List<AttrGroupEntity> attrGroupEntities = this.list(new QueryWrapper<AttrGroupEntity>().eq("catelog_id", cid));
        if (CollectionUtils.isEmpty(attrGroupEntities)) {
            return null;
        }
        //2遍历每个组下attr

        return  attrGroupEntities.stream().map(group -> {
                    ItemGroupVO itemGroupVO = new ItemGroupVO();
                    itemGroupVO.setGroupId(group.getAttrGroupId());
                    itemGroupVO.setGroupName(group.getAttrGroupName());
                    //通过分组的id查询中间表的信息
                    List<AttrAttrgroupRelationEntity> relationEntities = this.attrAttrgroupRelationDao.selectList(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_group_id", group.getAttrGroupId()));
                    //判断是否为空中间的数据是否为null
                    if (!CollectionUtils.isEmpty(relationEntities)) {
                        //通过中间表获取属性的attrId
                        List<Long> attIds = relationEntities.stream().map(AttrAttrgroupRelationEntity::getAttrId).collect(Collectors.toList());
                        //根据获取的属性attrId 和spuId 查询规格参数对应的值
                        List<ProductAttrValueEntity> productAttrValueEntities = this.productAttrValueDao.selectList(new QueryWrapper<ProductAttrValueEntity>().eq("spu_id", spuId).in("attr_id", attIds));
                        itemGroupVO.setBaseAttrValues(productAttrValueEntities);

                    }
                    return itemGroupVO;


                }).collect(Collectors.toList());

    }


}