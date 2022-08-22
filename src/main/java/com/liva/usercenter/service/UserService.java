package com.liva.usercenter.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.liva.usercenter.model.domain.User;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author liva
 * @description 针对表【user(用户)】的数据库操作Service
 * @createDate 2022-07-28 17:06:10
 */
public interface UserService extends IService<User> {

    /**
     * 用户注册
     *
     * @param userAccount   账户名
     * @param userPassword  密码
     * @param checkPassword 校验密码
     * @param planetCode    星球编号
     * @return
     */
    Long userRegister(String userAccount, String userPassword, String checkPassword, String planetCode);

    /**
     * 用户登录
     *
     * @param userAccount  账户名
     * @param userPassword 密码
     * @param request      http请求
     * @return
     */
    Long userLogin(String userAccount, String userPassword, HttpServletRequest request);


    /**
     * 用户查询
     *
     * @param username 用户名
     * @return
     */
    List<User> searchUsers(String username, HttpServletRequest request);

    /**
     * 删除用户
     *
     * @param id id
     * @return
     */
    Boolean deleteUser(long id, HttpServletRequest request);

    /**
     * 用户信息脱敏
     *
     * @param user
     * @return
     */
    User getSafetyUser(User user);

    /**
     * 获取当前用户
     *
     * @param request
     * @return
     */
    User currentUser(HttpServletRequest request);

    /**
     * 用户注销
     *
     * @param request
     * @return
     */
    Integer userLogout(HttpServletRequest request);

    /**
     * 根据标签搜索用户
     *
     * @param tagNameList
     * @return
     */
    List<User> searchUsersByTags(List<String> tagNameList);

    /**
     * 更新用户信息
     * @param user
     * @return
     */
    Boolean updateUser(User user, HttpServletRequest request);



    Page<User> usersRecommend(long pageSize, long pageNum, HttpServletRequest request);
}
