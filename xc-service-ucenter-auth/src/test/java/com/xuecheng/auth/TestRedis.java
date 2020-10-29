package com.xuecheng.auth;

import com.alibaba.fastjson.JSON;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

@SpringBootTest
@RunWith(SpringRunner.class)
public class TestRedis {
    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Test
    public void testRedis(){
        String key = "13a98254-19e0-4689-a363-bfe8f3fce191";
        HashMap<String, String> map = new HashMap<>();
        map.put("jwt","eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJjb21wYW55SWQiOm51bGwsInVzZXJwaWMiOm51bGwsInVzZXJfbmFtZSI6Iml0Y2FzdCIsInNjb3BlIjpbImFwcCJdLCJuYW1lIjpudWxsLCJ1dHlwZSI6bnVsbCwiaWQiOm51bGwsImV4cCI6MTYwMzczNzIzMiwianRpIjoiMTNhOTgyNTQtMTllMC00Njg5LWEzNjMtYmZlOGYzZmNlMTkxIiwiY2xpZW50X2lkIjoiWGNXZWJBcHAifQ.czPyht96e3-m--7usij0p0ON6rCfoqDR8I3hMvt2WNO3aBq4saXl8wdvXVFFDF7tk-CcUC_cbjF7E6wF5J07ZKo2J6jhwN7irB65bd9cw2L7yPqgc3HAAfe53wgD-PkFLTU2NTbNWYKfQOS3BVWjHEfF1ahAvlLw9OYGfsQyUZ6xFvLpbqJfL_Lx5--deuCfqUF_4iH3JjRre67y_SJH7zmEpHKwOlf0bxMZl6AACy0AjodHkrDyAfqKoewOtsPajBpu9VI0ye5bz5lnx4meKL3_qzPCZL9AXzEs2Z901Vuxonz8hDJWWN0L-wNsVCNQkQtfJpqLQ6ELCq_KSc5nFw");
        map.put("refresh_token","eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJjb21wYW55SWQiOm51bGwsInVzZXJwaWMiOm51bGwsInVzZXJfbmFtZSI6Iml0Y2FzdCIsInNjb3BlIjpbImFwcCJdLCJhdGkiOiIxM2E5ODI1NC0xOWUwLTQ2ODktYTM2My1iZmU4ZjNmY2UxOTEiLCJuYW1lIjpudWxsLCJ1dHlwZSI6bnVsbCwiaWQiOm51bGwsImV4cCI6MTYwMzczNzIzMiwianRpIjoiYjM1YjM4OWMtMjljNS00N2NiLTk0NWMtM2ZiYTdkMDkxOTM4IiwiY2xpZW50X2lkIjoiWGNXZWJBcHAifQ.gYFUUyZsuiPxdqNSOsvxHrM1YuH08MzjuzEVQEW3pkNfmGbtzKuRFi8Nk8pzXaX_xNMFeCh8aPgbKbIAJi1ZDw79NImQsjlNzGnQXy34cY_Y97pz1F-Gq8PDSXxDsmYzZkzkWOMHqQXZtPZhAy2Jkk2SBB6m2QQX2ekfEfRPo2sM9wdyq0IUMs21IRxObh-S87Ru_sWH9F3c_0zQAgjGkO1YSleVd9c986LImYu0eHg1_1FS-14sgXfhfRU5UoLD2OdQh2uupU9sNQsCCUh2kN04kBXTa6WdsPXzNjtY8pJ4PdvMEsCanyi2BNwtJNN1RNxGW_UnRN9txycwoTsoiA");
        String value = JSON.toJSONString(map);
        //存入
        stringRedisTemplate.boundValueOps(key).set(value,30, TimeUnit.SECONDS);
        //获取
        ValueOperations<String, String> operations = stringRedisTemplate.opsForValue();
        String des = operations.get(key);
        System.out.println(des);
    }


}
