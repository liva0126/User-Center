package com.liva.usercenter.exception;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.liva.usercenter.comment.BaseResponse;
import com.liva.usercenter.comment.ErrorCode;
import com.liva.usercenter.comment.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理类处理类
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)   //只去捕获这个异常
    public BaseResponse businessExceptionHandler(BusinessException e){
//        log.error("BusinessException :"+e.getMessage(),e);
        return R.error(e.getCode(),e.getMessage(),e.getDescription());
    }
    @ExceptionHandler(RuntimeException.class)   //只去捕获这个异常
    public BaseResponse runtimeExceptionHandler(RuntimeException e){
        log.error("RuntimeException :",e);
        return R.error(ErrorCode.SYSTEM_ERROR,e.getMessage(),"");
    }

}
