package com.liva.usercenter.model.request;

import lombok.Data;

/**
 * 通用分页请求参数封装类
 */
@Data
public class PageRequest {
    /**
     * 页面大小
     */
    protected int pageSize;

    /**
     * 当前是第几页
     */
    protected int pageNum;

}
