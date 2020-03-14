package com.atguigu.gmall.item.service;

import com.atguigu.gmall.item.vo.ItemVO;
import org.springframework.stereotype.Service;

@Service
public interface ItemService {

    public ItemVO queryItemBySkuId(Long skuId);
}
