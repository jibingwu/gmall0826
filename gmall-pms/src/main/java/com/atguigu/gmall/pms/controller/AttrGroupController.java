package com.atguigu.gmall.pms.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;


import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.QueryCondition;
import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.pms.vo.AttrGroupVO;
import com.atguigu.gmall.pms.vo.ItemGroupVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jdk.nashorn.internal.ir.ReturnNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.atguigu.gmall.pms.entity.AttrGroupEntity;
import com.atguigu.gmall.pms.service.AttrGroupService;


/**
 * 属性分组
 *
 * @author jsonwu
 * @email jsonwu@atguigu.com
 * @date 2020-02-18 17:27:03
 */
@Slf4j
@Api(tags = "属性分组 管理")
@RestController
@RequestMapping("pms/attrgroup")
public class AttrGroupController {
    @Autowired
    private AttrGroupService attrGroupService;

    //根据分类caId和spuid查询组及组下的规格参数

    @GetMapping("withattrvalues")
    public Resp<List<ItemGroupVO>> queryItemGroupsBySpuIdAndCid(@RequestParam("spuId" )Long spuId,@RequestParam("cid") Long cid){

        List<ItemGroupVO>  itemGroupVOS= this.attrGroupService.queryItemGroupsBySpuIdAndCid(spuId,cid);
        return Resp.ok(itemGroupVOS);


    }


    /**
     * http://127.0.0.1:8888/pms/attrgroup/withattrs/cat/225
     */
    /**
     * 参照接口文档：
     * 请求地址：/withattrs/cat/{catId}
     * 请求方式：GET
     * 请求参数：catId

     * 正确响应：`Resp<List<AttrGroupVO>>
     */

    @ApiOperation("通过分类的Id查询分类下的分组及其规格参数")
    @GetMapping("/withattrs/cat/{catId}")
    public  Resp<List<AttrGroupVO>>  queryByCid(@PathVariable("catId") Long catId){

       List<AttrGroupVO>  attrGroupVOs=   this.attrGroupService.queryByCid(catId);

       return Resp.ok(attrGroupVOs);

    }



    /**
     * 返回自定义的vo 返回的值包括分组表和属性表还有分组和属性表的中间表
     * @param gid
     * @return
     */
    @GetMapping("withattr/{gid}")

     public  Resp<AttrGroupVO>  queryById(@PathVariable("gid")  Long gid){
        AttrGroupVO  attrGroupVO=  this.attrGroupService.queryById(gid);
        log.debug("打印输出信息{}", attrGroupVO);
        return Resp.ok(attrGroupVO);
     }
    /**
     * 参数通过分类的id查询出三级分类和通用分页参数
     * 三级分类查询
     *
     * @param catid
     * @param condition
     * @return PageVo
     */

    @GetMapping("{catId}")
    public Resp<PageVo> queryByCidPage(@PathVariable("catId") Long catid, QueryCondition condition) {
        PageVo pageVo = this.attrGroupService.queryByCidPage(catid, condition);
        log.info("打印输出信息{}", pageVo);
        return Resp.ok(pageVo);
    }


    /**
     * 列表
     */
    @ApiOperation("分页查询(排序)")
    @GetMapping("/list")
    @PreAuthorize("hasAuthority('pms:attrgroup:list')")
    public Resp<PageVo> list(QueryCondition queryCondition) {
        PageVo page = attrGroupService.queryPage(queryCondition);

        return Resp.ok(page);
    }


    /**
     * 信息
     */
    @ApiOperation("详情查询")
    @GetMapping("/info/{attrGroupId}")
    @PreAuthorize("hasAuthority('pms:attrgroup:info')")
    public Resp<AttrGroupEntity> info(@PathVariable("attrGroupId") Long attrGroupId) {
        AttrGroupEntity attrGroup = attrGroupService.getById(attrGroupId);

        return Resp.ok(attrGroup);
    }

    /**
     * 保存
     */
    @ApiOperation("保存")
    @PostMapping("/save")
    @PreAuthorize("hasAuthority('pms:attrgroup:save')")
    public Resp<Object> save(@RequestBody AttrGroupEntity attrGroup) {
        attrGroupService.save(attrGroup);

        return Resp.ok(null);
    }

    /**
     * 修改
     */
    @ApiOperation("修改")
    @PostMapping("/update")
    @PreAuthorize("hasAuthority('pms:attrgroup:update')")
    public Resp<Object> update(@RequestBody AttrGroupEntity attrGroup) {
        attrGroupService.updateById(attrGroup);

        return Resp.ok(null);
    }

    /**
     * 删除
     */
    @ApiOperation("删除")
    @PostMapping("/delete")
    @PreAuthorize("hasAuthority('pms:attrgroup:delete')")
    public Resp<Object> delete(@RequestBody Long[] attrGroupIds) {
        attrGroupService.removeByIds(Arrays.asList(attrGroupIds));

        return Resp.ok(null);
    }

}
