package com.xuecheng.framework.exception;

import com.google.common.collect.ImmutableMap;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.framework.model.response.ResultCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

//统一异常捕获类
@ControllerAdvice
public class ExceptionCatch {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExceptionCatch.class);

    //非可预测异常对应的代码map
    private static ImmutableMap<Class<? extends Throwable>, ResultCode> EXCEPTIONS;
    //构建map,使用builder对象构建
    protected static ImmutableMap.Builder<Class<? extends Throwable>, ResultCode> builder =
            ImmutableMap.builder();

    //捕获CustomException时,调用此方法
    @ExceptionHandler(CustomException.class)
    @ResponseBody
    public ResponseResult customException(CustomException customException) {
        LOGGER.error("catch exception : {}\r\nexception: ", customException.getMessage(), customException);
        ResultCode resultCode = customException.getResultCode();
        return new ResponseResult(resultCode);
    }

    //捕获CustomException时,调用此方法
    @ExceptionHandler(Exception.class)
    @ResponseBody
    public ResponseResult customException(Exception exception) {
        LOGGER.error("catch exception : {}\r\nexception: ", exception.getMessage());
        if (EXCEPTIONS == null) {
            EXCEPTIONS = builder.build();//构建成功EXCEPTIONS
        }
        //从EXCEPTION中找错误代码,如果找到了,返回给用户,找不到的话响应99999异常
        ResultCode resultCode = EXCEPTIONS.get(exception.getClass());
        if (resultCode != null) {
            return new ResponseResult(resultCode);
        } else return new ResponseResult(CommonCode.SERVER_ERROR);
    }

    static {
        //定义异常类型对应的错误代码
        builder.put(HttpMessageNotReadableException.class, CommonCode.INVALID_PARAM);
    }
}
