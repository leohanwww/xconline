package com.xuecheng.govern.gateway.service;

import com.xuecheng.framework.utils.CookieUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@Service
public class LoginService {
    @Autowired
    StringRedisTemplate stringRedisTemplate;

    //从cookie查询用户身份令牌是否存在，不存在则拒绝访问
    public String getCookie(HttpServletRequest request) {
        Map<String, String> cookieMap = CookieUtil.readCookie(request, "uid");
        String accessToken = cookieMap.get("uid");
        if (StringUtils.isEmpty(accessToken)) {
            return null;
        }
        return accessToken;
    }

    //从http header查询jwt令牌是否存在，不存在则拒绝访问
    public String getJwtFromHeader(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        if (StringUtils.isEmpty(authorization)) {
            return null;
        }
        if (!authorization.startsWith("Bearer ")) {
            return null;
        }
        return authorization.substring(7);
    }

    //从Redis查询user_token令牌是否过期，过期则拒绝访问
    public boolean queryUIDFromRedis(String accessToken) {
        String key = "uid:"+accessToken;
        Long expire = stringRedisTemplate.getExpire(key);
        return expire > 0;
    }

}
