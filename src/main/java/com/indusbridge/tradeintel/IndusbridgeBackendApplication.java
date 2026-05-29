package com.indusbridge.tradeintel;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

// We are telling Spring to completely ignore the Database and Redis for now
@SpringBootApplication(exclude = {
    DataSourceAutoConfiguration.class, 
    RedisAutoConfiguration.class, 
    RedisRepositoriesAutoConfiguration.class
})
@ComponentScan(basePackages = "com.indusbridge.tradeintel")
public class IndusbridgeBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(IndusbridgeBackendApplication.class, args);
    }
}