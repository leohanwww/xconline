package com.xuecheng.auth;

import com.alibaba.fastjson.JSON;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.jwt.Jwt;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.jwt.crypto.sign.RsaSigner;
import org.springframework.security.jwt.crypto.sign.RsaVerifier;
import org.springframework.security.oauth2.provider.token.store.KeyStoreKeyFactory;
import org.springframework.test.context.junit4.SpringRunner;

import java.security.KeyPair;
import java.security.interfaces.RSAPrivateKey;
import java.util.HashMap;
import java.util.Map;

@SpringBootTest
@RunWith(SpringRunner.class)
public class TestJwt {

    @Test
    public void testCreateJwtToken(){
        //密钥库文件
        String key_location = "xc.keystore";
        // 密钥库的开启密码
        String keystore_password = "xuechengkeystore";
        // 密钥名称
        String alias = "xckey";
        // 密钥密码
        String key_password = "xuecheng";
        // 密钥库的位置
        ClassPathResource resource = new ClassPathResource(key_location);
        //密钥工程
        KeyStoreKeyFactory keyStoreKeyFactory = new KeyStoreKeyFactory(resource,keystore_password.toCharArray());
        // 得到密钥对(公钥和私钥)
        KeyPair keyPair = keyStoreKeyFactory.getKeyPair(alias, key_password.toCharArray());
        //获得私钥
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
        //生成jwt令牌
        //内容
        Map<String,String> map = new HashMap<>();
        map.put("name","itcast");
        String jsonString = JSON.toJSONString(map);
        Jwt jwt = JwtHelper.encode(jsonString, new RsaSigner(privateKey));
        //生成jwt令牌编码
        String jwtEncoded = jwt.getEncoded();
        System.out.println(jwtEncoded);
    }

    @Test
    public void verifyJwt(){
        String publickey = "-----BEGIN PUBLIC KEY-----MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAnASXh9oSvLRLxk901HANYM6KcYMzX8vFPnH/To2R+SrUVw1O9rEX6m1+rIaMzrEKPm12qPjVq3HMXDbRdUaJEXsB7NgGrAhepYAdJnYMizdltLdGsbfyjITUCOvzZ/QgM1M4INPMD+Ce859xse06jnOkCUzinZmasxrmgNV3Db1GtpyHIiGVUY0lSO1Frr9m5dpemylaT0BV3UwTQWVW9ljm6yR3dBncOdDENumT5tGbaDVyClV0FEB1XdSKd7VjiDCDbUAUbDTG1fm3K9sx7kO1uMGElbXLgMfboJ963HEJcU01km7BmFntqI5liyKheX+HBUCD4zbYNPw236U+7QIDAQAB-----END PUBLIC KEY-----";
        String jwt = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJjb21wYW55SWQiOiIxIiwidXNlcnBpYyI6bnVsbCwidXNlcl9uYW1lIjoiaXRjYXN0Iiwic2NvcGUiOlsiYXBwIl0sIm5hbWUiOiJ0ZXN0MDIiLCJ1dHlwZSI6IjEwMTAwMiIsImlkIjoiNDkiLCJleHAiOjE2MDQxODkxNzIsImF1dGhvcml0aWVzIjpbInhjX3RlYWNobWFuYWdlcl9jb3Vyc2VfYmFzZSIsInhjX3RlYWNobWFuYWdlcl9jb3Vyc2VfZGVsIiwieGNfdGVhY2htYW5hZ2VyX2NvdXJzZV9saXN0IiwieGNfdGVhY2htYW5hZ2VyX2NvdXJzZV9wbGFuIiwieGNfdGVhY2htYW5hZ2VyX2NvdXJzZSIsImNvdXJzZV9maW5kX2xpc3QiLCJ4Y190ZWFjaG1hbmFnZXIiLCJ4Y190ZWFjaG1hbmFnZXJfY291cnNlX21hcmtldCIsInhjX3RlYWNobWFuYWdlcl9jb3Vyc2VfcHVibGlzaCIsImNvdXJzZV9waWNfbGlzdCIsInhjX3RlYWNobWFuYWdlcl9jb3Vyc2VfYWRkIl0sImp0aSI6IjVmZTQ2Y2JjLTcwZDAtNGE0MC1iZmUzLTg1NWMzNjg2MWQ3ZCIsImNsaWVudF9pZCI6IlhjV2ViQXBwIn0.katGGYVBS4XwWZ6RwemP3-bdlC0GBSw8ZlyW-FB3mM_Vu1_TIsG-ZL0ATw7HHoegpVb6krOfuMDvQurNN9dRcGYr0TRIkp8qyG7j7c4OPAk54eZQu0Ad4sNUlPJszw-IkE4PqY3mJvqbe-GJovtV6tM8mE59PnWf5xsmLs046NY395LB52RfURMF3XzBT693evug-rgwSQor4SkgfPOUGetwbvSDs-juVBeVvhjX2wVe4YO4SkQv0jujzUEwzEhecS3duf_KP6sJJChfSF6Ybu3jdYT9KUMceyO_NvF7yn_P_v_iPfv7TikNnW5_C06n3LDewVmFKGnREl1Gq3OrdA";
        Jwt verify = JwtHelper.decodeAndVerify(jwt, new RsaVerifier(publickey));
        String claims = verify.getClaims();
        System.out.println(claims);
    }
}
