package com.demo.elasticsearch.demo.re;

import com.demo.elasticsearch.demo.pojo.User;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;


public interface UserRepository  extends ElasticsearchRepository<User,Long> {

    /**
     * 通过年龄范围
     * @param age1
     * @param age2
     * @return
     */
    List<User> findByAgeBetween(Integer age1,Integer age2);
     List<User>  findByNameOrAge (String name,Integer age);

    @Query("{\n" +
            "    \"range\": {\n" +
            "      \"age\": {\n" +
            "        \"gte\": \"?0\",\n" +
            "        \"lte\": \"?1\"\n" +
            "      }\n" +
            "    }\n" +
            "  }")
    List<User>  findByQuery (Integer age1,Integer age2);
}
