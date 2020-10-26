package com.gouzhong1223.wxmptest;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

@SpringBootTest
@RunWith(SpringRunner.class)
public class WxmptestApplicationTests {

    @Autowired
    private RedisTemplate redisTemplate;

    @Test
    public void test1() {
        HashMap<Object, Object> hashMap = new HashMap<>();
        hashMap.put("1", "2");
        redisTemplate.opsForValue().set("111", hashMap, 60L, TimeUnit.SECONDS);
        Object o = redisTemplate.opsForValue().get("111");
        System.out.println(o);
        redisTemplate.delete("111");

        Object o1 = redisTemplate.opsForValue().get("111");
        System.out.println(o1);

    }

}
