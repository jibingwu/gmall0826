package com.atguigu.gmall.search.listener;


import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.search.feign.GmallPmsFeign;
import com.atguigu.gmall.search.feign.GmallWmsFeig;
import com.atguigu.gmall.search.repository.GoodsRepository;
import com.atguigu.gmall.search.vo.GoodsVO;
import com.atguigu.gmall.search.vo.SearchAttrValueVO;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class SpuListener {

    //扩展sde
    @Autowired
    private GoodsRepository goodsRepository;

    @Autowired
    private GmallPmsFeign gmallPmsFeign;
    @Autowired
    private GmallWmsFeig gmallWmsFeig;


    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "SEARCH-ITEM-QUEUE", durable = "true", ignoreDeclarationExceptions = "true"),
            exchange = @Exchange(value = "PMS-SPU-EXCHANGE", ignoreDeclarationExceptions = "true", type = ExchangeTypes.TOPIC),key = {"item.insert"}
    ))
    public void Listener(Long spuId) {

        Resp<SpuInfoEntity> spuInfoEntityResp = this.gmallPmsFeign.querySpuById(spuId);
        SpuInfoEntity spuInfoEntity = spuInfoEntityResp.getData();
        if (spuInfoEntity == null) {
            return;
        }
        //通过spu的id查询出检索属性
        Resp<List<SkuInfoEntity>> skuResp = this.gmallPmsFeign.querySkusBySpuId(spuId);
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
    }


}