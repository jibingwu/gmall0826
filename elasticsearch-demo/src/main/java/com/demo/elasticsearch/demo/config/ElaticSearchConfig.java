package com.demo.elasticsearch.demo.config;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ElaticSearchConfig {


    @Bean
    RestHighLevelClient client(){
        return new RestHighLevelClient(
                RestClient.builder(
                        new HttpHost("175.24.46.5", 9200, "http")));
    }
}
