package com.atguigu.gmall.search;

import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.QueryCondition;
import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.search.feign.GmallPmsFeign;
import com.atguigu.gmall.search.feign.GmallWmsFeig;
import com.atguigu.gmall.search.repository.GoodsRepository;
import com.atguigu.gmall.search.vo.GoodsVO;
import com.atguigu.gmall.search.vo.SearchAttrValueVO;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

@SpringBootTest
class GmallSearchApplicationTests {


    //es客户端
    @Autowired
    private ElasticsearchRestTemplate restTemplate;

    //扩展sde
    @Autowired
    private GoodsRepository goodsRepository;


    @Autowired
    private GmallPmsFeign gmallPmsFeign;
    @Autowired
    private GmallWmsFeig gmallWmsFeig;


    @Test
    void contextLoads() {
        //创建索引
        this.restTemplate.createIndex(GoodsVO.class);
        //创建映射关系
        this.restTemplate.putMapping(GoodsVO.class);
        //分页查询已上架的SPU信息
        //从第一页数据开始
        Long pageNum = 1l;
        //每页查询100条
        Long pageSize = 100l;
        do {
            QueryCondition queryCondition = new QueryCondition();
            queryCondition.setPage(pageNum);
            queryCondition.setLimit(pageSize);
            Resp<List<SpuInfoEntity>> listResp = this.gmallPmsFeign.querySpuPage(queryCondition);
            //得到数据
            List<SpuInfoEntity> spuInfoEntities = listResp.getData();
            //通过spu查询spu下面的sku
            spuInfoEntities.forEach(spuInfoEntity -> {
                //通过spu的id查询出skuResp
                Resp<List<SkuInfoEntity>> skuResp = this.gmallPmsFeign.querySkusBySpuId(spuInfoEntity.getId());
                //获取到skuinfo数据
                List<SkuInfoEntity> skuInfoEntities = skuResp.getData();
                //判断集合是否为空，如果不为null就查询
                if (!CollectionUtils.isEmpty(skuInfoEntities)) {
                    //利用strem
                    List<GoodsVO> goodsVOs = skuInfoEntities.stream().map(skuInfoEntity -> {
                        //把sku的转换为goods
                        GoodsVO goodsVO = new GoodsVO();
                        goodsVO.setSkuId(skuInfoEntity.getSkuId());
                        goodsVO.setPic(skuInfoEntity.getSkuDefaultImg());
                        goodsVO.setTitle(skuInfoEntity.getSkuTitle());
                        goodsVO.setPrice(skuInfoEntity.getPrice().doubleValue());
                        goodsVO.setSale(null); //TODO
                        //根据分类id查询商品品牌
                        Long brandId = skuInfoEntity.getBrandId();
                        Resp<BrandEntity> brandEntityResp = this.gmallPmsFeign.queryBrandsById(brandId);
                        //获取到品牌的数据
                        BrandEntity brandEntity = brandEntityResp.getData();
                        //判断获取品牌id如果不等于null
                        if (brandEntity != null) {
                            goodsVO.setBrandId(brandId);
                            goodsVO.setBrandName(brandEntity.getName());
                        }
                        //根据品牌id查询商品分类
                        Long catalogId = skuInfoEntity.getCatalogId();
                        Resp<CategoryEntity> categoryEntityResp = this.gmallPmsFeign.info(catalogId);
                        CategoryEntity categoryEntity = categoryEntityResp.getData();
                        if (categoryEntity != null) {
                            goodsVO.setCategoryId(catalogId);
                            goodsVO.setCategoryName(categoryEntity.getName());
                        }
                        //设置商品创建时间
                        goodsVO.setCreateTime(spuInfoEntity.getCreateTime());

                        // 根据skuId查询库存信息
                       Resp<List<WareSkuEntity>> wareSkuResp = this.gmallWmsFeig.queryWareSkusBySkuId(skuInfoEntity.getSkuId());

                        List<WareSkuEntity> wareSkuEntities = wareSkuResp.getData();
                        if (!CollectionUtils.isEmpty(wareSkuEntities)) {
                            //库存值大于0，就设置库存
                            goodsVO.setStore(wareSkuEntities.stream().allMatch(wareSkuEntity -> wareSkuEntity.getStock() > 0));
                        }
                        //根据spuId查询检索规格参数及值
                        Resp<List<ProductAttrValueEntity>> listProductResp = this.gmallPmsFeign.queryAttrValueBySpuId(spuInfoEntity.getId());
                        List<ProductAttrValueEntity> attrValueEntities = listProductResp.getData();
                        if (!CollectionUtils.isEmpty(attrValueEntities)) {
                            List<SearchAttrValueVO> searchAttrVOS = attrValueEntities.stream().map(productAttrValueEntity -> {
                                SearchAttrValueVO searchAttrValueVO = new SearchAttrValueVO();
                                searchAttrValueVO.setAttrId(productAttrValueEntity.getAttrId());
                                searchAttrValueVO.setAttrName(productAttrValueEntity.getAttrName());
                                searchAttrValueVO.setAttrValue(productAttrValueEntity.getAttrValue());
                                return searchAttrValueVO;

                            }).collect(Collectors.toList());
                            //设置参数值
                            goodsVO.setAttrs(searchAttrVOS);
                        }
                        return goodsVO;
                    }).collect(Collectors.toList());
                    // 批量导入es中
                    System.out.println("goodsVOs = " + goodsVOs);
                    this.goodsRepository.saveAll(goodsVOs);
                }
            });
            pageSize = new Long(spuInfoEntities.size());
            pageNum++;

        } while (pageSize == 100);


    }

}
