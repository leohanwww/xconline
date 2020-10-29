package com.xuecheng.auth;

import com.xuecheng.framework.client.XcServiceList;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.Base64Utils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;
import java.util.Map;

@SpringBootTest
@RunWith(SpringRunner.class)
public class TestClient {
    @Autowired
    RestTemplate restTemplate;
    @Autowired
    LoadBalancerClient loadBalancerClient;

    //远程请求springsecurity服务获取token
    @Test
    public void testClient() {
        //从eureka得到微服务实例
        ServiceInstance serviceInstance = loadBalancerClient.choose(XcServiceList.XC_SERVICE_UCENTER_AUTH);
        URI uri = serviceInstance.getUri(); //  http://ip:port
        //http://localhost:40400/auth/oauth/token
        String authUri = uri + "/auth/oauth/token";
        //定义header
        LinkedMultiValueMap<String, String> header = new LinkedMultiValueMap<>();
        header.add("Authorization", getHttpBasic("XcWebApp", "XcWebApp"));
        //定义body
        LinkedMultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "password");
        body.add("username", "itcast");
        body.add("password", "123");


        HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<>(body, header);
        //设置resttemplate远程调用,对400和401不报错,正确返回数据
        restTemplate.setErrorHandler(new DefaultResponseErrorHandler() {
            @Override
            public void handleError(ClientHttpResponse response) throws IOException {
                if (response.getRawStatusCode() != 400 && response.getRawStatusCode() != 401) {
                    super.handleError(response);
                }

            }
        });
        //发起http请求,申请map格式的令牌
        ResponseEntity<Map> exchange = restTemplate.exchange(authUri, HttpMethod.POST, httpEntity, Map.class);
        Map bodyMap = exchange.getBody();
        System.out.println(bodyMap);

    }

    private String getHttpBasic(String clientId, String clientSecret) {
        String str = clientId + ":" + clientSecret;
        str = Base64Utils.encodeToString(str.getBytes());
        return "Basic " + str;
    }

    @Test
    public void testPasswrodEncoder() {
        String password = "111111";
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        for (int i = 0; i < 10; i++) {
            //每个计算出的Hash值都不一样
            String hashPass = passwordEncoder.encode(password);
            System.out.println(hashPass);
            //虽然每次计算的密码Hash值不一样但是校验是通过的
            boolean f = passwordEncoder.matches(password, hashPass);
            System.out.println(f);
        }
    }
}
