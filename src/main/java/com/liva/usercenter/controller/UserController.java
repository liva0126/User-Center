package com.liva.usercenter.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.liva.usercenter.comment.BaseResponse;
import com.liva.usercenter.comment.ErrorCode;
import com.liva.usercenter.comment.R;
import com.liva.usercenter.exception.BusinessException;
import com.liva.usercenter.mapper.UserMapper;
import com.liva.usercenter.model.domain.User;
import com.liva.usercenter.model.request.UserLoginRequest;
import com.liva.usercenter.model.request.UserRegisterRequest;
import com.liva.usercenter.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Base64;
import java.util.List;

/**
 * 用户控制层接口
 *
 * @author liva
 */

@RestController   //用于编写restful风格的api，类中所有的返回值类型都是application.json
@RequestMapping("/user")
@CrossOrigin(origins = "http://127.0.0.1:5173/",allowCredentials="true",allowedHeaders = "*")
public class UserController {

    @Resource
    private UserService userService;

    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        if (userRegisterRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        String planetCode = userRegisterRequest.getPlanetCode();
        //这里也做一下判断，controller倾向于针对数据本身的校验，不针对逻辑，如果数据本身有问题，就不调用service了
        //service层则是对业务逻辑的校验
        if (StringUtils.isAnyEmpty(userAccount, userPassword, checkPassword, planetCode)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long userId = userService.userRegister(userAccount, userPassword, checkPassword, planetCode);
        return R.success(userId, "用户:{" + userId + "}创建成功");
    }

    @PostMapping("/login")
    public BaseResponse<Long> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {

        if (userLoginRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();

        //这里也做一下判断，controller倾向于针对数据本身的校验，不针对逻辑，如果数据本身有问题，就不调用service了
        //service层则是对业务逻辑的校验
        if (StringUtils.isAnyEmpty(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        Long userId = userService.userLogin(userAccount, userPassword, request);
        return R.success(userId, "登录成功");
    }

    @PostMapping("/logout")
    public BaseResponse<Integer> userLogout(HttpServletRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Integer logout = userService.userLogout(request);
        return R.success(logout, "注销成功");
    }

    @GetMapping("/current")
    public BaseResponse<User> gerCurrentUser(HttpServletRequest request) {
        User user = userService.currentUser(request);
        return R.success(user);
    }

    @GetMapping("/search")
    public BaseResponse<List<User>> userSearch(String username, HttpServletRequest request) {
        List<User> users = userService.searchUsers(username, request);
        return R.success(users);
    }

    @GetMapping("/search/tags")
    public BaseResponse<List<User>> searchUserByTags(@RequestParam(required = false) List<String> tagNameList) {
        List<User> userList = userService.searchUsersByTags(tagNameList);
        return R.success(userList);
    }

    @GetMapping("/recommend")
    public BaseResponse<Page<User>> recommendUsers(long pageSize,long pageNum,HttpServletRequest request){
        Page<User> userList = userService.usersRecommend(pageSize, pageNum, request);
        return R.success(userList);
    }


    @PostMapping("/update")
    public BaseResponse<Boolean> userUpdate(@RequestBody User user,HttpServletRequest request){
        Boolean i = userService.updateUser(user,request);
        return R.success(i);
    }

    @GetMapping("/delete")
    public BaseResponse<Boolean> userDelete(long id, HttpServletRequest request) {
        Boolean b = userService.deleteUser(id, request);
        return R.success(b, "删除成功");
    }

}
