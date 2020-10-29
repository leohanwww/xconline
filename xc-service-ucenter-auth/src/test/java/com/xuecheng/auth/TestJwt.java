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
        String jwt = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJuYW1lIjoiaXRjYXN0In0.lQOqL1s4DpDHROUAibkz6EMf6hcM7HmTPgmg-SlkacVoQAV7y3XQ7LXxiua6SJlN_uNX_EFjzIshEg_kyy972DtymtRMc2NIO5HzIF5I4oQCxNPsJdhu6qQni6sTas3q0JbAarMZSajDX7HhzVSYWPQJCussA4e1r9oFxDcoAo6TEAXOW8gRHzNIygQz1yCj6mdf4UOHI070kRy7f3BdhmrUJdOuDIMoRBYS4WsEOibAU1UCNPaJAXpZC0ihrtdY7SCg1N43fimeFOHrfpLb6OmRF7v7uvGMgrhg9JIYDbJ6nbode5OJkNceRx8QUICre2yKAe0ctlvXO0REf6OpRA";
        Jwt verify = JwtHelper.decodeAndVerify(jwt, new RsaVerifier(publickey));
        String claims = verify.getClaims();
        System.out.println(claims);
    }
}
