package com.atguigu.gmall.pms.service.impl;

import ch.qos.logback.core.net.SyslogOutputStream;
import com.atguigu.gmall.pms.dao.*;
import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.pms.feign.SkuSaleFeign;
import com.atguigu.gmall.pms.service.*;
import com.atguigu.gmall.pms.vo.ProductAttrValueVO;
import com.atguigu.gmall.pms.vo.SkuInfoVO;
import com.atguigu.gmall.pms.vo.SpuInfoVO;
import com.atguigu.gmall.sms.vo.SkuSaleVO;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.Query;
import com.atguigu.core.bean.QueryCondition;

import org.springframework.util.CollectionUtils;

@Slf4j
@Service("spuInfoService")
public class SpuInfoServiceImpl extends ServiceImpl<SpuInfoDao, SpuInfoEntity> implements SpuInfoService {

    @Autowired
    private SpuInfoDescDao spuInfoDescDao;
    @Autowired
    private SpuInfoDescService spuInfoDescService;

    @Autowired
    private ProductAttrValueService productAttrValueService;

    @Autowired
    private  ProductAttrValueDao  productAttrValueDao;
    @Autowired
    private SkuInfoDao skuInfoDao;


    @Autowired
    private SkuImagesService skuImagesService;

    @Autowired
    private AttrDao attrDao;
    @Autowired
    private SkuSaleAttrValueDao saleAttrDao;

    @Autowired
    private SkuSaleFeign skuSaleFeign;
    @Autowired
    private SkuSaleAttrValueService skuSaleAttrValueService;

    @Autowired
    private AmqpTemplate amqpTemplate;

    @Override
    public PageVo queryPage(QueryCondition params) {
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                new QueryWrapper<SpuInfoEntity>()
        );

        return new PageVo(page);
    }

    @Override
    public PageVo querySpuInfo(QueryCondition condition, Long catId) {
        //构建分页查询条件
        IPage<SpuInfoEntity> page = new Query<SpuInfoEntity>().getPage(condition);

        //封装查询条件 catId
        QueryWrapper<SpuInfoEntity> wrapper = new QueryWrapper<>();
        //如果分类id不等于0,就按照id查询，否则就查询全部
        if (catId != 0) {
            wrapper.eq("catalog_id", catId);
        }
        //如果搜索了检索条件，就根据检索条件查询
        String key = condition.getKey();
        //如果不为null就按条件检错
        if (StringUtils.isNotBlank(key)) {
            //消费性
            wrapper.and(t -> t.eq("id", key).or().like("spu_name", key));
        }
        return new PageVo(this.page(page, wrapper));
    }

    /**
     * 保存spu的业务方法
     *
     * @param spuInfoVO
     */

    @GlobalTransactional
    @Override
    public void saveSpuInfoVO(SpuInfoVO spuInfoVO) {
        //---------------1.保存spu相关的信息--------------
        //1.1保存spu基本信息 spu_info  ps  创建时间  更新时间
        Long spuId = saveSpuInfo(spuInfoVO);
        //1.2spu的描述性信息  pms_spu_info_desc
        saveSpuDesc(spuInfoVO, spuId);
        //1.3保存spu的规格参数信息 pms_product_attr_value
        SaveProductAttrValue(spuInfoVO, spuId);
        // 最后制造异常
        //int i = 1 / 0;
        //------------2保存sku相关信息-------------------
        saveSkuInfoWithSaleInfo(spuInfoVO, spuId);
        //消息队列
        this.amqpTemplate.convertAndSend("PMS-SPU-EXCHANGE","item.insert",spuId);



    }

    /**
     *
     * 保存sku相关信息及营销信息
     * @param spuInfoVO
     * @param spuId
     */

    private void saveSkuInfoWithSaleInfo(SpuInfoVO spuInfoVO, Long spuId) {
        List<SkuInfoVO> skuInfoVOs = spuInfoVO.getSkus();
        if (CollectionUtils.isEmpty(skuInfoVOs)) {
            return;
        }
        skuInfoVOs.forEach(skuInfoVO -> {
            //2.1 保存基本信息
            SkuInfoEntity skuInfoEntity = new SkuInfoEntity();
            //把spuInfoVO有的拷贝给sku
            BeanUtils.copyProperties(skuInfoVO, skuInfoEntity);
            //品牌信息和分类的id需要从spuInfo获取
            skuInfoEntity.setBrandId(spuInfoVO.getBrandId());
            skuInfoEntity.setCatalogId(spuInfoVO.getCatalogId());
            //获取随机uuid给sku的code
            skuInfoEntity.setSkuCode(UUID.randomUUID().toString().substring(0, 10).toUpperCase());
            // 获取图片列表
            List<String> images = skuInfoVO.getImages();
            //如果不等于null就设置默认图片
            if (!CollectionUtils.isEmpty(images)) {
                //设置第一张图片给作为默认图片
                skuInfoEntity.setSkuDefaultImg(skuInfoEntity.getSkuDefaultImg() == null
                        ? images.get(0) : skuInfoEntity.getSkuDefaultImg());

            }
            //设置spuId
            skuInfoEntity.setSpuId(spuId);
            this.skuInfoDao.insert(skuInfoEntity);//调用dao方法保存sku基本属性
            //sku商品信息创建成功后。获取到sku的
            Long skuId = skuInfoEntity.getSkuId();

            //2.2保存sku的图片信息  pms_sku_images
            if (!CollectionUtils.isEmpty(images)) {
                //获取默认图片
                String defaultImage = images.get(0);

                List<SkuImagesEntity> skuImgaes = images.stream().map(image -> {
                    //创建sku图片保对象
                    SkuImagesEntity skuImagesEntity = new SkuImagesEntity();
                    skuImagesEntity.setDefaultImg(StringUtils.equals(defaultImage, image) ? 1 : 0);
                    skuImagesEntity.setSkuId(skuId);
                    skuImagesEntity.setImgSort(0);
                    skuImagesEntity.setImgUrl(image);

                    return skuImagesEntity;
                }).collect(Collectors.toList());
                //保存到数据中去
                this.skuImagesService.saveBatch(skuImgaes);
            }


            // 2.3. 保存sku的规格参数（销售属性）
            List<SkuSaleAttrValueEntity> saleAttrs = skuInfoVO.getSaleAttrs();
            //SkuSaleAttrValueEntity(id=null, skuId=null, attrId=33, attrName=null, attrValue=3000, attrSort=null)
            if (!CollectionUtils.isEmpty(saleAttrs)) {
                saleAttrs.forEach(SkuSaleAttrValueEntity -> {
                    //设置属性名，需要根据id查询 AttrEntity(attrDao)
                    // saleAttr.setAttrName(this.attrDao.selectById(saleAttr.getAttrId()).getAttrName());
                    SkuSaleAttrValueEntity.setAttrName(this.attrDao.selectById(SkuSaleAttrValueEntity.getAttrId()).getAttrName());
                    SkuSaleAttrValueEntity.setSkuId(skuId);
                 this.saleAttrDao.insert(SkuSaleAttrValueEntity);
                });


                }


            // --------------3、保存营销相关的相关信息，需要远程调用 （gmall-smm）-------
            // 3. 保存营销相关信息，需要远程调用gmall-sms
            SkuSaleVO skuSaleVO = new SkuSaleVO();
            //拷贝，在设置skuId
            BeanUtils.copyProperties(skuInfoVO, skuSaleVO);
            skuSaleVO.setSkuId(skuId);

            this.skuSaleFeign.saveSkuSaleInfo(skuSaleVO);


        });
    }

    /**
     * 保存SaveProductAttrValue基本属性信息
     * @param spuInfoVO
     * @param spuId
     */
    private void SaveProductAttrValue(SpuInfoVO spuInfoVO, Long spuId) {
        List<ProductAttrValueVO> baseAttrs = spuInfoVO.getBaseAttrs();
        if (!CollectionUtils.isEmpty(baseAttrs)) {
            baseAttrs.forEach(ProductAttrValueVO->{
                ProductAttrValueVO.setSpuId(spuId);
                //批量保存到数据中去
                this.productAttrValueDao.insert(ProductAttrValueVO);
            });

        }
    }

    /**
     * spu的描述性信息  pms_spu_info_desc
     * @param spuInfoVO
     * @param spuId
     */
    private void saveSpuDesc(SpuInfoVO spuInfoVO, Long spuId) {
        //把商品的图片描述，保存到spu详细中，图片以逗号进行分割
        List<String> supImages = spuInfoVO.getSupImages();
        System.out.println(supImages);
        SpuInfoDescEntity spuInfoDescEntity = new SpuInfoDescEntity();
        spuInfoDescEntity.setSpuId(spuId); //信息表的spuId设置给描述表
        spuInfoDescEntity.setDecript(StringUtils.join(supImages, ","));

        this.spuInfoDescService.save(spuInfoDescEntity); //保存到数据库中
    }


    /**
     * 保存
     * @param spuInfoVO  保存spu基本信息 spu_info
     * @return
     */
    private Long saveSpuInfo(SpuInfoVO spuInfoVO) {
        spuInfoVO.setPublishStatus(1); //默认上架
        spuInfoVO.setCreateTime(new Date()); //创建时间
        spuInfoVO.setUodateTime(spuInfoVO.getCreateTime());//更新时间和创建时间一样
        this.save(spuInfoVO); //调用mp的save方法
        return spuInfoVO.getId();
    }

}