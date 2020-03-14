package com.atguigu.gmall.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.nacos.client.config.utils.ContentUtils;
import com.atguigu.core.bean.Query;
import com.atguigu.gmall.search.service.SearchServices;
import com.atguigu.gmall.search.vo.GoodsVO;
import com.atguigu.gmall.search.vo.SearchParamVO;
import com.atguigu.gmall.search.vo.SearchResponseAttrVO;
import com.atguigu.gmall.search.vo.SearchResponseVO;
import javafx.scene.chart.BarChart;
import lombok.SneakyThrows;
import net.sf.jsqlparser.statement.truncate.Truncate;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
public class SearchServiceImpl implements SearchServices {

    //注入es高级客户端
    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @Override
    public SearchResponseVO search(SearchParamVO searchParamVO) {

        try {
            //构建sql语句 第一个参数是构建的索引，第二个是构建的dsl语句
            SearchRequest searchRequest = new SearchRequest(new String[]{"goods"}, buildDsl(searchParamVO));
            SearchResponse response = this.restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
            //返回解析的数据//抽取方法
            SearchResponseVO searchResponseVO = parseResult(response);

            //分页


        } catch (IOException e) {
            e.printStackTrace();
        }


        return null;
    }


    /**
     * 构建dsl语句,根据用户构建发送的参数构建查询条件
     *
     * @param searchParamVO
     * @return
     */
    private SearchSourceBuilder buildDsl(SearchParamVO searchParamVO) {
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        //1、构建查询条件和过滤条件
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        sourceBuilder.query(boolQueryBuilder);

        //1.1构建匹配查询
        String keyword = searchParamVO.getKeyword();
        if (StringUtils.isEmpty(keyword)) {
            //TODO 也可打广告
            //直接返回
            return null;
        }
        //如果匹配不等于null must matchQuery operator
        boolQueryBuilder
                .must(QueryBuilders.matchQuery("title", keyword).operator(Operator.AND));

        //1.2构建过滤条件
        //1.2.1按照品牌过滤
        Long[] brandId = searchParamVO.getBrandId();
        if (brandId != null && brandId.length != 0) {
            boolQueryBuilder.filter(QueryBuilders.termsQuery("brandId", brandId));

        }
        //1.2.2构建分类的过滤
        Long[] categoryId = searchParamVO.getCategoryId();
        if (categoryId != null && categoryId.length != 0) {
            boolQueryBuilder.filter(QueryBuilders.termsQuery("categoryId", categoryId));
        }
        //1.2.3构建价格
        Double priceFrom = searchParamVO.getPriceFrom();
        Double priceTo = searchParamVO.getPriceTo();
        if (priceFrom != null || priceTo != null) {
            RangeQueryBuilder rangeQuery = QueryBuilders.rangeQuery("price");
            if (priceFrom != null) {
                rangeQuery.gte(priceFrom);
            }
            if (priceTo != null) {
                rangeQuery.lte(priceTo);
            }

            boolQueryBuilder.filter(rangeQuery);
        }
        //1.24构建是否有货
        Boolean store = searchParamVO.getStore();
        if (store != null) { //todo
            boolQueryBuilder.filter(QueryBuilders.termsQuery("store", store));
        }
        //1.2.5. 构建规格参数的嵌套过滤
        String[] props = searchParamVO.getProps();
        if (props != null && props.length != 0) {
            //截取规格参数(设置请求格式)
            for (String prop : props) { // 33:1-2
                String[] attr = StringUtils.split(prop, ":");
                //判断用户传入的参数是否合法是否合法
                if (attr == null || attr.length != 2) {
                    //跳过本次循环执行下一下循环
                    continue;
                }
                BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
                boolQuery.must(QueryBuilders.termsQuery("attrs.attrId", attr[0]));//获取第一个规格参数id
                boolQuery.must(QueryBuilders.termsQuery("attrs.attrValue", StringUtils.split(attr[1], "-")));
                boolQueryBuilder.filter(QueryBuilders.nestedQuery("attrs", boolQuery, ScoreMode.None));
            }
        }
        //2.构建排序 order=1:desc (0:得分 1：价格 2：销量 3：新品)
        String order = searchParamVO.getOrder();
        if (StringUtils.isNotBlank(order)) {
            String[] sorts = StringUtils.split(order, ",");
            if (sorts != null && sorts.length == 2) {

                String sortFiled = "_score";
                switch (sorts[0]) {
                    case "1":
                        sortFiled = "price";
                        break;
                    case "2":
                        sortFiled = "sale";
                        break;
                    case "3":
                        sortFiled = "createTime";
                        break;
                    default:
                        break;
                }
                sourceBuilder.sort(sortFiled, StringUtils.equals("desc", sorts[1]) ? SortOrder.DESC : SortOrder.ASC);

            }


        }
        //3.。构建分页
        Integer pageNum = searchParamVO.getPageNum();
        Integer pageSize = searchParamVO.getPageSize();
        sourceBuilder.from((pageNum - 1) * pageSize);
        sourceBuilder.size(pageSize);

        //4构建高亮
        sourceBuilder.highlighter(new HighlightBuilder()
                .field("title")
                .preTags("<font style='color:red'>")
                .postTags("</font>")
        );
        //5.构建聚合
        //5.1
        sourceBuilder.aggregation(AggregationBuilders.terms("brandIdAgg").field("brandId").subAggregation(AggregationBuilders.terms("brandNameAgg").field("brandName")));
        //5.2分类聚合
        sourceBuilder.aggregation(AggregationBuilders.terms("categoryIdAgg").field("categoryId").subAggregation(AggregationBuilders.terms("categoryNameAgg").field("categoryName")));

        //5.3规格参数的聚合
        sourceBuilder.aggregation(AggregationBuilders.terms("attrsAgg").field("attrsAgg").subAggregation(AggregationBuilders.terms("attrIdAgg").field("attrs.attrId"))
                .subAggregation(AggregationBuilders.terms("attrNameAgg").field("attrs.attrName")).subAggregation(AggregationBuilders.terms("attrValueAgg").field("attrs.attrValue"))
        );
         //6.添加结果集过滤，只包含需要的商品列表字段
        sourceBuilder.fetchSource(new String[]{"skuId","title","price","pic"},null);
        System.out.println("sourceBuilder = " + sourceBuilder.toString());
        return sourceBuilder;


    }


    //用来解析结果dsl
    private SearchResponseVO parseResult(SearchResponse response) {
        System.out.println("rsponse = " + response);
        //创建 搜索响应VO来存取构建的值
        SearchResponseVO responseVO = new SearchResponseVO();
         //通过SearchResponse构建的dsl获取需要响应的信息
        SearchHits hits = response.getHits();
        //获取总记录数
        responseVO.setTotal(hits.getTotalHits());
        //解析 hits获取查询记录
        SearchHit[] hitsHits = hits.getHits();
        //用来存取 hitsHit得到的数据
        List<GoodsVO> goodsVOS = new ArrayList<>();
        //遍历hitsHits数组，把hitsHits遍历转换成goodsVO对象，存取到集合里
        for (SearchHit hitsHit : hitsHits) {
            String  goodsVOJson = hitsHit.getSourceAsString();
            //把获取的_source(goodsVOJson)以json数据格式添加到集合当中
            GoodsVO goodsVO = JSON.parseObject(goodsVOJson, GoodsVO.class);
            //获取高亮结果集
            HighlightField highlightField = hitsHit.getHighlightFields().get("title");
            Text fragment = highlightField.getFragments()[0];
            //把高亮的结果设置给goodsVO
             goodsVO.setTitle(fragment.string());
             goodsVOS.add(goodsVO);
            // 最终把goodsVOS集合中数据设置给responseVO
        }

        responseVO.setData(goodsVOS);


        // 解析聚合结果集获取品牌
            Map<String, Aggregation> aggsMap = response.getAggregations().getAsMap();
            
            //获取品牌的集合，并强转成可解析的聚合品牌
            ParsedLongTerms brandIdAgg = (ParsedLongTerms)aggsMap.get("brandIdAgg");
            //获取【品牌】聚合下所有的桶   一个聚合下有多个桶
            List<? extends Terms.Bucket> buckets = brandIdAgg.getBuckets();
            //如果buckets得到的桶，不等于null就解析
             if(!CollectionUtils.isEmpty(buckets)){

                 List<String> bradndValues = buckets.stream().map(bucket -> {
                     HashMap<Object, Object> map = new HashMap<>();
                     //通过bucket遍历获取到桶的id，设置给map集合保存
                     long brandId = bucket.getKeyAsNumber().longValue();
                     map.put("id", brandId);
                     //通过bucket获取下brandNameAgg聚合的
                     ParsedStringTerms brandNameAgg = (ParsedStringTerms) bucket.getAggregations().get("brandNameAgg");
                     //通过brandNameAgg获取到get(0)第一个聚合的值
                     String brandName = brandNameAgg.getBuckets().get(0).getKeyAsString();
                     map.put("name", brandName);
                     //然后把map转换成json
                     return JSON.toJSONString(map);

                 }).collect(Collectors.toList());
                 SearchResponseAttrVO brandVo = new SearchResponseAttrVO();
                 brandVo.setAttrName("品牌");
                 brandVo.setAttrValues(bradndValues);
                 responseVO.setBrand(brandVo);
             }


        // 解析聚合结果集获取分类
         //通过aggsMap获取categoryIdAgg聚合
        ParsedLongTerms  categoryIdAgg=  (ParsedLongTerms) aggsMap.get("categoryIdAgg");
           //通过categoryIdAgg集合获取聚合下的桶
        List<? extends Terms.Bucket> categoryIdAggBuckets = categoryIdAgg.getBuckets();
        //判断桶是否null，为空，就不操作
        if(!CollectionUtils.isEmpty(categoryIdAggBuckets)){
            List<String> categoryValues = categoryIdAggBuckets.stream().map(bucket -> {
                //创建一个map集合保存分类的id和name
                HashMap<Object, Object>  map = new HashMap<>();
                long categoryId = ((Terms.Bucket) bucket).getKeyAsNumber().longValue();
                map.put("id",categoryId);
                ParsedStringTerms   categoryNameAgg=  (ParsedStringTerms)bucket.getAggregations().get("categoryNameAgg");
                String   categoryName= categoryNameAgg.getBuckets().get(0).getKeyAsString();
                map.put("name",categoryName);
                //然后把map集合转成json
                return JSON.toJSONString(map);

            }).collect(Collectors.toList());
            SearchResponseAttrVO  categoryVO = new SearchResponseAttrVO();
            categoryVO.setAttrName("分类");
            categoryVO.setAttrValues(categoryValues);
            responseVO.setCategory(categoryVO);
        }

        // 解析聚合结果集获取规格参数
            ParsedNested    attrsAgg=  (ParsedNested)aggsMap.get("attrsAgg");
            //获取attrsAgg下的子集合
            ParsedLongTerms attrIdAgg = (ParsedLongTerms)attrsAgg.getAggregations().get("attrIdAgg");
            //获取子聚合下的桶
            List<? extends Terms.Bucket> attrIdAggBuckets = attrIdAgg.getBuckets();
                    //判断桶是佛为nulll
                    if(!CollectionUtils.isEmpty(attrIdAggBuckets)){
                        List<SearchResponseAttrVO> attrVOList = attrIdAggBuckets.stream().map(bucket -> {
                            //创建vo对象设置给attrsVO
                            SearchResponseAttrVO attrVO = new SearchResponseAttrVO();
                            long AttrId = ((Terms.Bucket) bucket).getKeyAsNumber().longValue();
                            attrVO.setAttrId(AttrId);
                            ParsedStringTerms  attrNmaeAgg = ((Terms.Bucket) bucket).getAggregations().get("attrNmaeAgg");
                            String AttrName = attrNmaeAgg.getBuckets().get(0).getKeyAsString();
                            attrVO.setAttrName(AttrName);


                            ParsedStringTerms  attrValueAgg = ((Terms.Bucket) bucket).getAggregations().get("attrValueAgg");
                            //获取谁有的桶attrValueAgg
                            List<? extends Terms.Bucket>  attrValueBuckets = attrValueAgg.getBuckets();
                             if(!CollectionUtils.isEmpty(attrValueBuckets)){
                                 List<String> attrValues = attrIdAggBuckets.stream().map(Terms.Bucket::getKeyAsString).collect(Collectors.toList());
                                 attrVO.setAttrValues(attrValues);
                             }
                    return attrVO;

                }).collect(Collectors.toList());

                //最后把返回的结果设置给respVo
                responseVO.setAttrs(attrVOList);

            }
        return responseVO;
    }

}
