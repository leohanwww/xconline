package com.xuecheng.auth.service;

import com.alibaba.fastjson.JSON;
import com.xuecheng.framework.client.XcServiceList;
import com.xuecheng.framework.domain.ucenter.ext.AuthToken;
import com.xuecheng.framework.domain.ucenter.response.AuthCode;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.ResponseResult;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Service;
import org.springframework.util.Base64Utils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class AuthService {

    @Autowired
    RestTemplate restTemplate;
    @Autowired
    LoadBalancerClient loadBalancerClient;
    @Autowired
    StringRedisTemplate stringRedisTemplate;
    @Value("${auth.tokenValiditySeconds}")
    int tokenValiditySeconds;

    public AuthToken login(String username, String password, String clientId, String clientSecret) {
        //申请令牌
        AuthToken authToken = this.applyToken(username, password, clientId, clientSecret);
        if (authToken == null) {
            ExceptionCast.cast(AuthCode.AUTH_LOGIN_APPLYTOKEN_FAIL);
        }
        //将令牌存到redis
        String access_token = authToken.getAccess_token();
        String content = JSON.toJSONString(authToken);
        boolean result = saveToken(access_token, content, tokenValiditySeconds);
        if (!result) {
            ExceptionCast.cast(AuthCode.AUTH_LOGIN_SAVETOKEN_FAIL);
        }
        return authToken;
    }

    //从redis查询令牌
    public AuthToken getUserToken(String token) {
        String key = "uid:" + token;
        String tokenString = (String) stringRedisTemplate.opsForValue().get(key);
        try {
            AuthToken authToken = JSON.parseObject(tokenString, AuthToken.class);
            return authToken;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * description
     *
     * @param access_token 用户身份令牌
     * @param content      authToken
     * @param ttl          生存时间
     * @return boolean
     */
    private boolean saveToken(String access_token, String content, long ttl) {
        //令牌名称
        String key = "uid:" + access_token;
        //保存到令牌到redis
        stringRedisTemplate.opsForValue().set(key, content, ttl, TimeUnit.SECONDS);
        //stringRedisTemplate.boundValueOps(key).set(content, ttl, TimeUnit.SECONDS);
        //获取过期时间
        Long expire = stringRedisTemplate.getExpire(key);
        return expire > 0;
    }

    private AuthToken applyToken(String username, String password, String clientId, String clientSecret) {
        //从eureka得到微服务实例
        ServiceInstance serviceInstance = loadBalancerClient.choose(XcServiceList.XC_SERVICE_UCENTER_AUTH);
        URI uri = serviceInstance.getUri(); //  http://ip:port
        //http://localhost:40400/auth/oauth/token
        String authUri = uri + "/auth/oauth/token";
        //定义header
        LinkedMultiValueMap<String, String> header = new LinkedMultiValueMap<>();
        header.add("Authorization", getHttpBasic(clientId, clientSecret));
        //定义body
        LinkedMultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "password");
        body.add("username", username);
        body.add("password", password);

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
        if (bodyMap == null ||
                bodyMap.get("access_token") == null ||
                bodyMap.get("refresh_token") == null ||
                bodyMap.get("jti") == null) {
            String error_description = (String) bodyMap.get("error_description");
            if (StringUtils.isNotEmpty(error_description)) {
                ExceptionCast.cast(AuthCode.AUTH_CREDENTIAL_ERROR);
            }
        }
        AuthToken authToken = new AuthToken();
        authToken.setAccess_token((String) bodyMap.get("jti")); // 用户身份短令牌
        authToken.setRefresh_token((String) bodyMap.get("refresh_token")); //刷新令牌
        authToken.setJwt_token((String) bodyMap.get("access_token")); // 访问令牌
        return authToken;
    }

    private String getHttpBasic(String clientId, String clientSecret) {
        String str = clientId + ":" + clientSecret;
        str = Base64Utils.encodeToString(str.getBytes());
        return "Basic " + str;
    }

    public ResponseResult logout() {
        //从cookie删除token
        //
        //从redis删除token
        return new ResponseResult(CommonCode.SUCCESS);
    }

    //从redis删除token
    public boolean delToken(String uid) {
        String key = "uid:" + uid;
        stringRedisTemplate.delete(key);
        return true;
    }
}
