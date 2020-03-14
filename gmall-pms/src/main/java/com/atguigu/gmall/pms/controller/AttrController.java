package com.atguigu.gmall.pms.controller;

import java.util.Arrays;
import java.util.Map;


import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.QueryCondition;
import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.pms.vo.AttrVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.atguigu.gmall.pms.entity.AttrEntity;
import com.atguigu.gmall.pms.service.AttrService;




/**
 * 商品属性
 *
 * @author jsonwu
 * @email jsonwu@atguigu.com
 * @date 2020-02-18 17:27:03
 */
@Api(tags = "商品属性 管理")
@RestController
@RequestMapping("pms/attr")
public class AttrController {
    @Autowired
    private AttrService attrService;


    /**
     * 参考接口文档：
     * 请求地址：无，已经在AttrController上配置/pms/attr
     * 请求方式：GET
     * 请求参数：cid type  QueryCondition
     * 正确响应：Resp<PageVo>
     *
     *  查询出规格下的分组后面的属性
     *  例如：手机的分组其中一个
     *   分组属属性     分组下的属性属性      分组属性的值
     *   主体          入网型号 （多个）           以官网信息为准（多个）
     *
     *
     *       */
    /**
     *
     * @param condition
     * @param cid   是否是分类的 id ，
     * @param type  是否是 销售属性还是普通属性（0，1）
     * @return
     */
    @GetMapping
    public  Resp<PageVo> queryByCidTypePage(
             QueryCondition condition,
             @RequestParam("cid") Long cid,
             @RequestParam(value="attr_type",required = false) Integer type){
         PageVo  pageVo=  this.attrService.queryByCidTypePage(condition,cid,type);
         return  Resp.ok(pageVo);
    }


    /**
     * 列表
     */
    @ApiOperation("分页查询(排序)")
    @GetMapping("/list")
    @PreAuthorize("hasAuthority('pms:attr:list')")
    public Resp<PageVo> list(QueryCondition queryCondition) {
        PageVo page = attrService.queryPage(queryCondition);

        return Resp.ok(page);
    }


    /**
     * 信息
     */
    @ApiOperation("详情查询")
    @GetMapping("/info/{attrId}")
    @PreAuthorize("hasAuthority('pms:attr:info')")
    public Resp<AttrEntity> info(@PathVariable("attrId") Long attrId){
		AttrEntity attr = attrService.getById(attrId);

        return Resp.ok(attr);
    }

    /**
     * 保存
     */
    @ApiOperation("保存")
    @PostMapping("/save")
    @PreAuthorize("hasAuthority('pms:attr:save')")
    public Resp<Object> save(@RequestBody AttrVO attrVO){
		this.attrService.saveAttrVO(attrVO);

        return Resp.ok(null);
    }

    /**
     * 修改
     */
    @ApiOperation("修改")
    @PostMapping("/update")
    @PreAuthorize("hasAuthority('pms:attr:update')")
    public Resp<Object> update(@RequestBody AttrEntity attr){
		attrService.updateById(attr);

        return Resp.ok(null);
    }

    /**
     * 删除
     */
    @ApiOperation("删除")
    @PostMapping("/delete")
    @PreAuthorize("hasAuthority('pms:attr:delete')")
    public Resp<Object> delete(@RequestBody Long[] attrIds){
		attrService.removeByIds(Arrays.asList(attrIds));

        return Resp.ok(null);
    }

}
