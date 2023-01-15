package com.itheima;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import javax.annotation.Resource;

@SpringBootTest
class ReggieTakeoutApplicationTests {
    @Resource
    private RedisTemplate<String,Object> redisTemplate;

    @Test
    void redisTest() {
        redisTemplate.opsForValue().set("age","22");
    }

}
