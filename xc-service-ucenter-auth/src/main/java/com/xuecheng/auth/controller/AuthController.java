package com.xuecheng.auth.controller;

import com.xuecheng.api.auth.AuthControllerApi;
import com.xuecheng.auth.service.AuthService;
import com.xuecheng.framework.domain.ucenter.ext.AuthToken;
import com.xuecheng.framework.domain.ucenter.request.LoginRequest;
import com.xuecheng.framework.domain.ucenter.response.AuthCode;
import com.xuecheng.framework.domain.ucenter.response.JwtResult;
import com.xuecheng.framework.domain.ucenter.response.LoginResult;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.framework.utils.CookieUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

@RestController
public class AuthController implements AuthControllerApi {
    @Value("${auth.clientId}")
    String clientId;
    @Value("${auth.clientSecret}")
    String clientSecret;
    @Value("${auth.cookieDomain}")
    String cookieDomain;
    @Value("${auth.cookieMaxAge}")
    int cookieMaxAge;
    @Value("${auth.tokenValiditySeconds}")
    int tokenValiditySeconds;

    @Autowired
    AuthService authService;

    /**
     * 用户登录
     * description
     *
     * @param loginRequest
     * @return com.xuecheng.framework.domain.ucenter.response.LoginResult
     */
    @Override
    @PostMapping("/userlogin")
    public LoginResult login(LoginRequest loginRequest) {
        if (loginRequest == null || StringUtils.isEmpty(loginRequest.getUsername())) {
            ExceptionCast.cast(AuthCode.AUTH_USERNAME_NONE);
        }
        if (loginRequest == null || StringUtils.isEmpty(loginRequest.getPassword())) {
            ExceptionCast.cast(AuthCode.AUTH_PASSWORD_NONE);
        }

        String username = loginRequest.getUsername();
        String password = loginRequest.getPassword();
        //申请token
        AuthToken authToken = authService.login(username, password, clientId, clientSecret);
        //将token存到cookie
        String accessToken = authToken.getAccess_token(); //拿到短身份令牌
        saveCookie(accessToken);
        return new LoginResult(CommonCode.SUCCESS, accessToken);
    }

    private void saveCookie(String token) {
        HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getResponse();
       /* HttpServletResponse response,String domain,String path, String name,
                String value, int maxAge,boolean httpOnly*/
        CookieUtil.addCookie(response, cookieDomain, "/", "uid", token, cookieMaxAge, false);
    }

    /**
     * 用户推出登录
     * description
     * @param
     * @return com.xuecheng.framework.model.response.ResponseResult
     */
    @Override
    @PostMapping("/userlogout")
    public ResponseResult logout() {
        //取出身份令牌
        String uid = getCookieToken();
        //删除redis中token
        boolean delToken = authService.delToken(uid);
        //清除cookie
        clearCookie(uid);
        return new ResponseResult(CommonCode.SUCCESS);
    }

    private void clearCookie(String token) {
        HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getResponse();
       /* HttpServletResponse response,String domain,String path, String name,
                String value, int maxAge,boolean httpOnly*/
        CookieUtil.addCookie(response, cookieDomain, "/", "uid", token, 0, false);
    }


    @Override
    @GetMapping("/userjwt")
    public JwtResult userjwt() {
        //取出cookie
        String uid = getCookieToken(); //身份令牌
        if (uid == null) {
            return new JwtResult(CommonCode.FAIL, null);
        }
        //redis查询jwt令牌
        AuthToken userToken = authService.getUserToken(uid);
        if (userToken != null) {
            String jwt_token = userToken.getJwt_token();
            return new JwtResult(CommonCode.SUCCESS, jwt_token);
        }
        return null;
    }

    //取出cookie中的身份令牌
    public String getCookieToken() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        Map<String, String> cookie = CookieUtil.readCookie(request, "uid");
        if (cookie != null && cookie.get("uid") != null) {
            String accessToken = cookie.get("uid");
            return accessToken;
        }
        return null;
    }
}
