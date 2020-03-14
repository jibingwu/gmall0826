package com.atguigu.gmall.item.service.impl;

import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.item.feign.GmallPmsFeign;
import com.atguigu.gmall.item.feign.GmallSmsFeign;
import com.atguigu.gmall.item.feign.GmallWmsFeign;
import com.atguigu.gmall.item.service.ItemService;
import com.atguigu.gmall.item.vo.ItemVO;
import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.pms.vo.ItemGroupVO;
import com.atguigu.gmall.sms.vo.ItemSaleVO;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.*;

@Service
public class ItemServiceImpl implements ItemService {

    @Autowired
    private GmallPmsFeign pmsFeign;
    @Autowired
    private GmallSmsFeign smsFeign;
    @Autowired
    private GmallWmsFeign wmsFeign;

    @Autowired
    private ThreadPoolExecutor threadPoolExecutor;


    @Override
    public ItemVO queryItemBySkuId(Long skuId) {
        ItemVO itemVO = new ItemVO();
        //查询sku
        CompletableFuture<SkuInfoEntity> skuFuture = CompletableFuture.supplyAsync(() -> {
            Resp<SkuInfoEntity> skuInfoEntityResp = this.pmsFeign.querysSkuById(skuId);
            SkuInfoEntity skuInfoEntity = skuInfoEntityResp.getData();
            if (skuInfoEntity == null) {
                return null;
            }
            itemVO.setSkuId(skuId);
            itemVO.setSkuTitle(skuInfoEntity.getSkuTitle());
            itemVO.setSkuSubTtile(skuInfoEntity.getSkuSubtitle());
            itemVO.setPrice(skuInfoEntity.getPrice());
            itemVO.setWeight(skuInfoEntity.getWeight());
            return skuInfoEntity;

        });

        
        //营销信息
        CompletableFuture<Resp<List<ItemSaleVO>>> saleFuture = CompletableFuture.supplyAsync(() -> {
            Resp<List<ItemSaleVO>> listResp = this.smsFeign.queryItemSaleBySkuId(skuId);
            List<ItemSaleVO> itemSaleVOList = listResp.getData();
            itemVO.setSales(itemSaleVOList);
            return listResp;
        }, threadPoolExecutor);

        //库存信息
        CompletableFuture<List<WareSkuEntity>> wareSkuFuture = CompletableFuture.supplyAsync(() -> {
            Resp<List<WareSkuEntity>> wareSkuResp = this.wmsFeign.queryWareSkusBySkuId(skuId);
            List<WareSkuEntity> wareSkuEntities = wareSkuResp.getData();
            if (!CollectionUtils.isEmpty(wareSkuEntities)) {
                boolean flag = wareSkuEntities.stream().anyMatch(wareSkuEntity -> wareSkuEntity.getStock() > 0);
                itemVO.setStore(flag);
            }
            return wareSkuEntities;

        }, threadPoolExecutor);


        //sku图片信息
        CompletableFuture<Resp<List<SkuImagesEntity>>> skuImagesFuture = CompletableFuture.supplyAsync(() -> {
            Resp<List<SkuImagesEntity>> SkuImagesResp = this.pmsFeign.querySkuImagesBySkuId(skuId);
            List<SkuImagesEntity> SkuImagesEntities = SkuImagesResp.getData();
            itemVO.setImages(SkuImagesEntities);
            return SkuImagesResp;
        }, threadPoolExecutor);

        //品牌信息
        CompletableFuture<Void> brandFuture = skuFuture.thenAcceptAsync(skuInfoEntity -> {
            Resp<BrandEntity> brandEntityResp = this.pmsFeign.queryBrandsById(skuInfoEntity.getBrandId());
            BrandEntity brandEntity = brandEntityResp.getData();
            if (brandEntity != null) {
                itemVO.setBrandId(brandEntity.getBrandId());
                itemVO.setBrandName(brandEntity.getName());
            }
        });
        

        
        //分类信息
        CompletableFuture<Void>   categoryFuture = skuFuture.thenAcceptAsync(skuInfoEntity -> {
            Resp<CategoryEntity> CategoryEntityResp = this.pmsFeign.info(skuInfoEntity.getCatalogId());
            CategoryEntity categoryEntity = CategoryEntityResp.getData();
            if (categoryEntity != null) {
                itemVO.setCategoryId(categoryEntity.getCatId());
                itemVO.setCategoryName(categoryEntity.getName());
            }
        });




        //spu信息
        CompletableFuture<Void> spuInfoFuture = skuFuture.thenAcceptAsync(skuInfoEntity -> {
            Resp<SpuInfoEntity> spuInfoEntityResp = this.pmsFeign.querySpuById(skuInfoEntity.getSpuId());
            SpuInfoEntity spuInfoEntity = spuInfoEntityResp.getData();
            if (spuInfoEntity != null) {
                itemVO.setSpuId(spuInfoEntity.getId());
                itemVO.setSpuName(spuInfoEntity.getSpuName());
            }

        });
        



        //销售属性
        CompletableFuture<Void> skuSaleAttrValueFuture = skuFuture.thenAcceptAsync(skuInfoEntity -> {
            Resp<List<SkuSaleAttrValueEntity>> SkuSaleAttrResp = this.pmsFeign.querySaleAttrBySpuId(skuInfoEntity.getSpuId());
            List<SkuSaleAttrValueEntity> SkuSaleAttrValueEntities = SkuSaleAttrResp.getData();
            itemVO.setSaleAttrs(SkuSaleAttrValueEntities);

        });

        //组以及组下一下规格参数值
        CompletableFuture<Void> groupFuture = skuFuture.thenAcceptAsync(skuInfoEntity -> {
            Resp<List<ItemGroupVO>> ItemGroupResp = this.pmsFeign.queryItemGroupsBySpuIdAndCid(skuInfoEntity.getSpuId(), skuInfoEntity.getCatalogId());
            List<ItemGroupVO> ItemGroupVO = ItemGroupResp.getData();
            itemVO.setGroups(ItemGroupVO);
        });
        


        //spu描述信息
        CompletableFuture<Void> descFuture = skuFuture.thenAcceptAsync(skuInfoEntity -> {
            Resp<SpuInfoDescEntity> spuInfoDescEntityResp = this.pmsFeign.querySkuDescBySpuId(skuInfoEntity.getSpuId());
            SpuInfoDescEntity spuInfoDescEntity = spuInfoDescEntityResp.getData();
            System.out.println("spuInfoDescEntity = " + spuInfoDescEntity);
            if (spuInfoDescEntity != null) {
                String[] split = StringUtils.split(spuInfoDescEntity.getDecript(), ",");
                itemVO.setDesc(Arrays.asList(split));
            }
        });
        CompletableFuture.allOf(saleFuture,wareSkuFuture,skuImagesFuture,brandFuture,categoryFuture,spuInfoFuture,skuSaleAttrValueFuture,groupFuture,descFuture).join();
        System.out.println("itemVO = " + itemVO);
        return itemVO;
    }
}
