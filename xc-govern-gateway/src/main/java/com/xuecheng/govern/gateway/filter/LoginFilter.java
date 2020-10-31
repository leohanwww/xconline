package com.xuecheng.govern.gateway.filter;

import com.alibaba.fastjson.JSON;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.govern.gateway.service.LoginService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

//身份过滤器
@Component
public class LoginFilter extends ZuulFilter {
    @Autowired
    LoginService loginService;

    @Override
    public String filterType() {
        /*
        * pre：请求在被路由之前执行
        * routing：在路由请求时调用
        * post：在routing和errror过滤器之后调用
        * error：处理请求时发生错误调用
        * */
        return "pre";
    }

    //过滤器序号,越小越优先
    @Override
    public int filterOrder() {
        return 0;
    }

    @Override
    public boolean shouldFilter() {
        return true;
    }

    //过虑所有请求，判断头部信息是否有Authorization，如果没有则拒绝访问，否则转发到微服务。
    @Override
    public Object run() throws ZuulException {
        RequestContext requestContext = RequestContext.getCurrentContext();
        HttpServletRequest request = requestContext.getRequest();
        HttpServletResponse response = requestContext.getResponse();
        String cookie = loginService.getCookie(request);
        if (StringUtils.isEmpty(cookie)){
            denyAccess();
            return null;
        }
        String authorization = loginService.getJwtFromHeader(request);
        if (StringUtils.isEmpty(authorization)){
            denyAccess();
            return null;
        }

        boolean b = loginService.queryUIDFromRedis(cookie);
        if (!b){
            denyAccess();
            return null;
        }
        return null;
    }

    //拒绝访问
    public void denyAccess(){
        RequestContext requestContext = RequestContext.getCurrentContext();
        HttpServletResponse response = requestContext.getResponse();
        //拒绝访问
        requestContext.setSendZuulResponse(false);
        //设置返回信息
        requestContext.setResponseStatusCode(200);
        ResponseResult responseResult = new ResponseResult(CommonCode.UNAUTHENTICATED);
        //转成json
        String jsonString = JSON.toJSONString(responseResult);
        requestContext.setResponseBody(jsonString);
        //设置contentType
        response.setContentType("application/json;charset=utf-8");

    }
}
