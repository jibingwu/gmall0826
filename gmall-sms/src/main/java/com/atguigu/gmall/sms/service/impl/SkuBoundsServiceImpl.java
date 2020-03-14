package com.atguigu.gmall.sms.service.impl;

        import com.atguigu.gmall.sms.dao.SkuFullReductionDao;
        import com.atguigu.gmall.sms.dao.SkuLadderDao;
        import com.atguigu.gmall.sms.entity.SkuFullReductionEntity;
        import com.atguigu.gmall.sms.entity.SkuLadderEntity;
        import com.atguigu.gmall.sms.vo.ItemSaleVO;
        import com.atguigu.gmall.sms.vo.SkuSaleVO;
        import org.springframework.beans.BeanUtils;
        import org.springframework.beans.factory.annotation.Autowired;
        import org.springframework.stereotype.Service;

        import java.math.BigDecimal;
        import java.util.ArrayList;
        import java.util.List;

        import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
        import com.baomidou.mybatisplus.core.metadata.IPage;
        import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
        import com.atguigu.core.bean.PageVo;
        import com.atguigu.core.bean.Query;
        import com.atguigu.core.bean.QueryCondition;

        import com.atguigu.gmall.sms.dao.SkuBoundsDao;
        import com.atguigu.gmall.sms.entity.SkuBoundsEntity;
        import com.atguigu.gmall.sms.service.SkuBoundsService;
        import org.springframework.transaction.annotation.Transactional;
        import org.springframework.util.CollectionUtils;


@Service("skuBoundsService")
public class SkuBoundsServiceImpl extends ServiceImpl<SkuBoundsDao, SkuBoundsEntity> implements SkuBoundsService {


    //减满
    @Autowired
    private SkuFullReductionDao skuFullReductionDao;

    //打折
    @Autowired
    private SkuLadderDao skuLadderDao;

    @Override
    public PageVo queryPage(QueryCondition params) {
        IPage<SkuBoundsEntity> page = this.page(
                new Query<SkuBoundsEntity>().getPage(params),
                new QueryWrapper<SkuBoundsEntity>()
        );

        return new PageVo(page);
    }


    /**
     * sku营销
     *
     * @param skuSaleVO
     */

    @Transactional
    @Override
    public void saveSkuSaleInfo(SkuSaleVO skuSaleVO) {
        //3.1 积分优惠
        SkuBoundsEntity skuBoundsEntity = new SkuBoundsEntity();
        BeanUtils.copyProperties(skuSaleVO, skuBoundsEntity);
        //数据库保存的work 是整数0-15 页面是 0000 - 11111 - 1010
        List<Integer> work = skuSaleVO.getWork();
        //8 4 2 1  从
        if (!CollectionUtils.isEmpty(work)) {
            skuBoundsEntity.setWork(work.get(0) * 8 + work.get(1) * 4 + work.get(2) * 2 + work.get(3));
        }
        //保存到数据库中
        this.save(skuBoundsEntity);


        //3.2满减优惠
        SkuFullReductionEntity skuFullReductionEntity = new SkuFullReductionEntity();
        BeanUtils.copyProperties(skuSaleVO, skuFullReductionEntity);
        skuFullReductionEntity.setAddOther(skuSaleVO.getFullAddOther());
        this.skuFullReductionDao.insert(skuFullReductionEntity);

        //3.3数量打折
        SkuLadderEntity skuLadderEntity = new SkuLadderEntity();
        BeanUtils.copyProperties(skuSaleVO, skuLadderEntity);
        this.skuLadderDao.insert(skuLadderEntity);
    }


    @Override
    public List<ItemSaleVO> queryItemSaleBySkuId(Long skuId) {
        //创建一个集合保存积分、打折、满减
        List<ItemSaleVO> itemSaleVOS = new ArrayList<>();
        // 积分
        SkuBoundsEntity skuBoundsEntity = this.getOne(new QueryWrapper<SkuBoundsEntity>().eq("sku_id", skuId));
        if (skuBoundsEntity != null) {
            //创建ItemSaleVO 设置值
            ItemSaleVO itemSaleVO = new ItemSaleVO();
            itemSaleVO.setType("积分");
            itemSaleVO.setDesc("成长积分赠送：" + skuBoundsEntity.getBuyBounds() + "购物积赠送：" + skuBoundsEntity.getBuyBounds());
            //添加到集合当中
            itemSaleVOS.add(itemSaleVO);
        }

        // 满减:SkuFullReductionEntity
        SkuFullReductionEntity skuFullReductionEntity = this.skuFullReductionDao.selectOne(new QueryWrapper<SkuFullReductionEntity>().eq("sku_id", skuId));
        if (skuFullReductionEntity != null) {
            //创建 ItemSaleVO
            ItemSaleVO itemSaleVO = new ItemSaleVO();
            itemSaleVO.setType("满减");
            itemSaleVO.setDesc("满" + skuFullReductionEntity.getFullPrice() + "减" + skuFullReductionEntity.getReducePrice());
            itemSaleVOS.add(itemSaleVO);
        }


        // 打折SkuLadderEntity
        SkuLadderEntity skuLadderEntity = this.skuLadderDao.selectOne(new QueryWrapper<SkuLadderEntity>().eq("sku_id", skuId));
        if(skuLadderEntity!=null){
             ItemSaleVO itemSaleVO=new ItemSaleVO();
              itemSaleVO.setDesc("打折");
            itemSaleVO.setDesc("满" + skuLadderEntity.getFullCount() + "件打" + skuLadderEntity.getDiscount().divide(new BigDecimal(10)) + "折");

            itemSaleVOS.add(itemSaleVO);
        }
         //返回
        return itemSaleVOS;
    }

}