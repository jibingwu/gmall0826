package com.atguigu.gmall.pms.api;

import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.QueryCondition;
import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.pms.vo.CategoryVO;
import com.atguigu.gmall.pms.vo.ItemGroupVO;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import java.util.List;

public interface GmallPmsApi {

    //- 分页查询已上架的SPU信息
    @PostMapping("pms/spuinfo/page")
    public Resp<List<SpuInfoEntity>> querySpuPage(@RequestBody QueryCondition queryCondition);

    //- 根据SpuId查询对应的SKU信息（接口已写好）
    @GetMapping("pms/skuinfo/{spuId}")
    public Resp<List<SkuInfoEntity>> querySkusBySpuId(@PathVariable("spuId") Long spuId);
    //通过skuId查询sku信息 1
    @GetMapping("pms/skuinfo/info/{skuId}")//info
    public Resp<SkuInfoEntity> querysSkuById(@PathVariable("skuId") Long skuId);

    //- 根据品牌id查询品牌（逆向工程已自动生成）3
    @GetMapping("pms/brand/infos/{Id}")
    public Resp<BrandEntity> queryBrandsById(@PathVariable("Id") Long id);

    //根据分类id查询商品分类（逆向工程已自动生成）2
    @GetMapping("pms/category/info/{catId}")
    public Resp<CategoryEntity> info(@PathVariable("catId") Long catId);

    //通过 skuid查询 spu信息4 GetMapping("pms/skuinfo/info/{skuId}")
    @GetMapping("pms/skuinfo/info/{id}")
    public Resp<SpuInfoEntity> querySkuById(@PathVariable("id") Long id);

    //通过skuId查询sku图片信息 8
    @GetMapping("pms/skuimages/{skuId}")
    public Resp<List<SkuImagesEntity>> querySkuImagesBySkuId(@PathVariable("skuId") Long skuId);

    //通过spuId查询spu详细信息 9
    @GetMapping("pms/spuinfodesc/info/{spuId}")
    public Resp<SpuInfoDescEntity> querySkuDescBySpuId(@PathVariable("spuId") Long spuId);

    @GetMapping("pms/spuinfo/info/{id}")
    public Resp<SpuInfoEntity> querySpuById(@PathVariable("id") Long id);

    @GetMapping("pms/skusaleattrvalue/{spuId}")
    public Resp<List<SkuSaleAttrValueEntity>> querySaleAttrBySpuId(@PathVariable("spuId") Long spuId);

     //通过
    @GetMapping("pms/skusaleattrvalue/skuId/{skuId}")
    public Resp<List<SkuSaleAttrValueEntity>>  querySkuBySkuId(@PathVariable("skuId") Long skuId);


    //根据等级或者父类id查询分类
    @GetMapping("pms/category")
    public Resp<List<CategoryEntity>> queryCategory(@RequestParam(value = "level", defaultValue = "0") Integer level
            , @RequestParam(value = "parentCid", required = false) Long parentCid);

    @GetMapping("pms/category/{pid}")
    public Resp<List<CategoryVO>> queryCategoryWithSubByPid(@PathVariable("pid") Long pid);


    //- 根据spuId查询检索规格参数及值
    @GetMapping("pms/productattrvalue/{spuId}")
    public Resp<List<ProductAttrValueEntity>> queryAttrValueBySpuId(@PathVariable("spuId") Long spuId);

    //根据分类caId和spuid查询组及组下的规格参数10
    @GetMapping("pms/attrgroup/withattrvalues")
    public Resp<List<ItemGroupVO>> queryItemGroupsBySpuIdAndCid(@RequestParam("spuId" )Long spuId, @RequestParam("cid") Long cid);


}
