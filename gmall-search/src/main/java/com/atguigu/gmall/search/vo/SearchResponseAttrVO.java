package com.atguigu.gmall.search.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
public class SearchResponseAttrVO implements Serializable {

    private Long attrId;

    private String attrName;

    private List<String> attrValues;



}
