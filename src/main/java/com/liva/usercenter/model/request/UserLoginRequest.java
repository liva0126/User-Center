package com.liva.usercenter.model.request;

import lombok.Data;
import java.io.Serializable;

/**
 * 用户登录请求体
 * @author liva
 *
 */

@Data
public class UserLoginRequest implements Serializable{
    private static final long serialVersionUID = -5076980379773437442L;

    private String userAccount;
    private String userPassword;

}







