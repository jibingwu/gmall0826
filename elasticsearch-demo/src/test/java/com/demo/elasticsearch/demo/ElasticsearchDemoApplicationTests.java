package com.demo.elasticsearch.demo;

import ch.qos.logback.core.net.SyslogOutputStream;
import com.alibaba.fastjson.JSON;
import com.demo.elasticsearch.demo.pojo.User;
import com.demo.elasticsearch.demo.re.UserRepository;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.security.SetUserEnabledRequest;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.annotation.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;

import javax.naming.directory.SearchResult;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static sun.misc.Version.println;

@SpringBootTest
class ElasticsearchDemoApplicationTests {
    @Autowired
    UserRepository userRepository;

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @Test
    void  RestHighLevelClientTest() throws IOException {
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.matchQuery("name","冰冰"));
        searchSourceBuilder.highlighter(new HighlightBuilder().field("name").preTags("<em>").postTags("</em>"));
        searchSourceBuilder.sort("age",SortOrder.ASC);
        searchSourceBuilder.from(1);
        searchSourceBuilder.size(1);
        


        SearchRequest user = new SearchRequest(new String[]{"user"}, searchSourceBuilder);
        //SearchResponse search(SearchRequest searchRequest, RequestOptions options)
        SearchResponse search = this.restHighLevelClient.search(user, RequestOptions.DEFAULT);

        SearchHits hits = search.getHits();
        for (SearchHit hit : hits) {
              //_source
            String sourceAsString = hit.getSourceAsString();
            //反序列化为user
            User user1 = JSON.parseObject(sourceAsString, User.class);
            //获取高亮结果
            Map<String, HighlightField> highlightFields = hit.getHighlightFields();
            HighlightField highlightField = highlightFields.get("name");
            //获取高亮的结果名称
            Text fragment = highlightField.getFragments()[0];
            //把获取到高亮的结果集设置给user1
            user1.setName(fragment.toString());

            System.out.println(user1);
        }

        

    }



    /*
    要 RestHighLevel官方推荐
       TransportClien在7开始要抛弃掉，停止维护
     */
    // ElasticsearchTemplate是TransportClient客户端
    // ElasticsearchRestTemplate是RestHighLevel客户端
    @Autowired
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    @Test
    void contextLoads() {
        //创建索引
        this.elasticsearchRestTemplate.createIndex(User.class);
        //创建对应的关系mapping
        this.elasticsearchRestTemplate.putMapping(User.class);
        //删除索引 user
        //this.elasticsearchRestTemplate.deleteIndex("user");
    }

    @Test
    void add() {
        /**
         *  @Id
         *     private  int id;
         *     @Field(type = FieldType.Text,analyzer = "ik_max_word")
         *     private   String name;
         *     @Field(type = FieldType.Integer)
         *     private Integer age;
         *     @Field(type = FieldType.Keyword)
         *     private String password;
         */


        this.userRepository.save(new User(2l, "zs", 18, "123456"));


    }

    @Test
    void delete() {

        this.userRepository.deleteById(1l);

    }

    @Test
    void testAddAll() {
        List<User> users = new ArrayList<>();
        users.add(new User(1l, "柳岩", 18, "123456"));
        users.add(new User(2l, "范冰冰", 19, "123456"));
        users.add(new User(3l, "李冰冰", 20, "123456"));
        users.add(new User(4l, "锋哥", 21, "123456"));
        users.add(new User(5l, "小鹿", 22, "123456"));
        users.add(new User(6l, "韩红", 23, "123456"));
        this.userRepository.saveAll(users);
    }

    @Test
    void findAage() {
        List<User> byAgeBetween = this.userRepository.findByAgeBetween(20, 30);
        System.out.println(byAgeBetween);


    }

    @Test
    void findNameAndAge() {
        System.out.println(this.userRepository.findByNameOrAge("韩红", 20));

    }
    @Test
    void findNameAndAge02(){
        System.out.println(this.userRepository.findByQuery(20,30));
    }


    @Test
     void  userSort(){
        this.userRepository.findAll(Sort.by(new Sort.Order(Sort.Direction.DESC,"age"))).forEach(System.out::println);

    }


    @Test
    void  rePository(){
        //构建查询search
        //this.userRepository.search(QueryBuilders.matchQuery("name","冰冰")).forEach(System.out::println);

        //自定义构建器
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        //根据名字找找
       // queryBuilder.withQuery(QueryBuilders.matchQuery("name","冰冰"));
        //范围查询找
        queryBuilder.withQuery(QueryBuilders.rangeQuery("age").gte(20).lte(30));
        //排序
        queryBuilder.withSort(SortBuilders.fieldSort("age").order(SortOrder.ASC));
        //分页
        queryBuilder.withPageable(PageRequest.of(1,2));
        //高亮
        //queryBuilder.withHighlightBuilder(new HighlightBuilder().field("name").preTags("<em>").postTags("</em>"));

        //执行自定义构建器
       this.userRepository.search(queryBuilder.build()).forEach(System.out::println);
    }

}
