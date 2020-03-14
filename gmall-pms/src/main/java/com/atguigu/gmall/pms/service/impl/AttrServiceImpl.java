package com.atguigu.gmall.pms.service.impl;

import com.atguigu.gmall.pms.dao.AttrAttrgroupRelationDao;
import com.atguigu.gmall.pms.entity.AttrAttrgroupRelationEntity;
import com.atguigu.gmall.pms.vo.AttrVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import net.bytebuddy.asm.Advice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.Query;
import com.atguigu.core.bean.QueryCondition;

import com.atguigu.gmall.pms.dao.AttrDao;
import com.atguigu.gmall.pms.entity.AttrEntity;
import com.atguigu.gmall.pms.service.AttrService;
import org.springframework.util.StringUtils;


@Service("attrService")
public class AttrServiceImpl extends ServiceImpl<AttrDao, AttrEntity> implements AttrService {
    @Autowired
    private  AttrDao attrDao;

    @Autowired
    private AttrAttrgroupRelationDao attrAttrgroupRelationDao;

    @Override
    public PageVo queryPage(QueryCondition params) {
        IPage<AttrEntity> page = this.page(
                new Query<AttrEntity>().getPage(params),
                new QueryWrapper<AttrEntity>()
        );

        return new PageVo(page);
    }


    /**
     * 查询规格参数下的属性
     * @param condition
     * @param cid
     * @param type
     * @return
     */
    @Override
    public PageVo queryByCidTypePage(QueryCondition condition, Long cid, Integer type) {
         //构造建查询条件
        //通过 分类的id查询出 不许不能为空。只能使 0或者是1
        QueryWrapper<AttrEntity>  wrapper = new QueryWrapper<>();
        wrapper.eq("catelog_id",cid);
         //是否是销售属性还还是普通属性。如果等于null，不等于null查询出 ，等于null 什么都不查询
        if(type !=null){
            wrapper.eq("attr_type",type);
        }
         // 通过 page（）方法查询 返回一个 Ipage对象，第一个分页参数，第二是查询的条件
        IPage<AttrEntity> pageVo = this.page(new Query<AttrEntity>().getPage(condition),
                wrapper);
        //把查询的结果 转换成PageVo工具类，返回湖出去
        return new PageVo(pageVo) ;
    }


    /**
     * 插入一条规格参数数据
     * @param attrVO
     */
    @Override
    public void saveAttrVO(AttrVO attrVO) {
        //新增规格参数（属性）
        this.attrDao.insert(attrVO);
        //新增中间表
        AttrAttrgroupRelationEntity relation = new AttrAttrgroupRelationEntity();
        relation.setAttrId(attrVO.getAttrId());
        relation.setAttrGroupId(attrVO.getAttrGroupId());
        //插入一条道中间表
        this.attrAttrgroupRelationDao.insert(relation);

    }

}