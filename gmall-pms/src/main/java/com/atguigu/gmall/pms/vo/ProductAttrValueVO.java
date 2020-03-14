package com.atguigu.gmall.pms.vo;

import com.alibaba.nacos.client.naming.utils.StringUtils;
import com.atguigu.gmall.pms.entity.ProductAttrValueEntity;
import lombok.Data;
import org.springframework.util.CollectionUtils;

import java.util.List;

public class ProductAttrValueVO extends ProductAttrValueEntity {
    public void setValueSelected(List<Object> valueSelected){
        // 如果接受的集合为空，则不设置
        if (CollectionUtils.isEmpty(valueSelected)){
            return;
        }
        this.setAttrValue(StringUtils.join(valueSelected, ","));
    }

}
