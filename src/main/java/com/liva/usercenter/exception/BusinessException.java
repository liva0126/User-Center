package com.liva.usercenter.exception;

import com.liva.usercenter.comment.ErrorCode;
import lombok.Data;

/**
 * 自定义业务异常
 */

@Data
public class BusinessException extends RuntimeException{

    private static final long serialVersionUID = 600945607257830440L;

    private int code;

    private String description;

    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.code = errorCode.getCode();
    }
    public BusinessException(int code,String message,String description) {
        super(message);
        this.code = code;
        this.description = description;
    }
    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
        this.description = errorCode.getDescription();
    }
}
