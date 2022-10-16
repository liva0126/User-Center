package com.liva.usercenter.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.liva.usercenter.exception.BusinessException;
import com.liva.usercenter.comment.ErrorCode;
import com.liva.usercenter.model.domain.User;
import com.liva.usercenter.service.UserService;
import com.liva.usercenter.mapper.UserMapper;
import com.liva.usercenter.utils.EncodeUtils;
import com.liva.usercenter.utils.RegexUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.liva.usercenter.comment.Constant.*;

/**
 * @author liva
 * @description 针对表【user(用户)】的数据库操作Service实现
 * @createDate 2022-07-28 17:06:10
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private static final String USER_LOGIN_STATE = "userLoginState";
    private static final String SALT = "liva";
    private static final int ADMIN_ROLE = 1;

    @Resource
    private UserMapper userMapper;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private RedisTemplate redisTemplate;


    @Override
    public Long userRegister(String userAccount, String userPassword, String checkPassword, String planetCode) {
        //判断合法
        if (StringUtils.isAnyEmpty(userAccount, userPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户或密码不合法");
        }
        //判断长度
        if (userAccount.length() < 4 || userPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户或密码不合法");
        }
        //判断账户是否合法
        if (RegexUtils.isSpecialChar(userAccount)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户或密码不合法");
        }
        //校验两次密码是否相同
        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次密码输入不一致");
        }
        if (planetCode.length() > 5) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "星球码错误");
        }
        //对密码加密
        //DigestUtils.md5DigestAsHex((salt+userPassword).getBytes(StandardCharsets.UTF_8))
        String encodePassword = EncodeUtils.encode(userPassword, SALT);

        //查询数据库是否有重复账户
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        int count = userMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "用户不存在");
        }

        //查询数据库是否有重复星球编号
        queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("planetCode", planetCode);
        count = userMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "星球码不合法");
        }
        //添加用户
        User user = new User();

        user.setUserAccount(userAccount);
        user.setUserPassword(encodePassword);
        user.setPlanetCode(planetCode);

        boolean save = this.save(user);
        if (!save) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "保存失败");

        }
        return user.getId();
    }

    @Override
    public Long userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        //判断合法
        if (StringUtils.isAnyEmpty(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //判断长度
        if (userAccount.length() < 4 || userPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //判断账户是否合法
        if (RegexUtils.isSpecialChar(userAccount)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //对密码加密
        String encodePassword = EncodeUtils.encode(userPassword, SALT);
        //查询数据库是否存在用户
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        queryWrapper.eq("userPassword", encodePassword);
        User user = userMapper.selectOne(queryWrapper);

        if (user == null) {
            log.info("user cannot found");
            throw new BusinessException(ErrorCode.NULL_ERROR, "用户名或密码错误");
        }

        //用户信息脱敏
        User safetyUser = getSafetyUser(user);

        // 记录用户登录状态
        request.getSession().setAttribute(USER_LOGIN_STATE, safetyUser);

        return safetyUser.getId();
    }

    @Override
    public List<User> searchUsers(String username, HttpServletRequest request) {
        // 鉴权，仅管理员可查询
        if (!isAdmin(request)) {
            return null;
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        if (StringUtils.isBlank(username)) {
            return null;
        }
        queryWrapper.like("username", username);
        List<User> userList = this.list(queryWrapper);
        if (userList.isEmpty()) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "查询不到该用户");
        }

        userList.stream().map(this::getSafetyUser).collect(Collectors.toList());

        return userList;
    }

    @Override
    public Boolean deleteUser(long id, HttpServletRequest request) {
        //鉴权，仅管理员可查询
        if (!isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        if (id < 0) {
            log.info("id cannot found");
        }
        return this.removeById(id);
    }

    /**
     * 用户信息脱敏
     * @param user
     * @return
     */
    @Override
    public User getSafetyUser(User user) {
        if (user == null) {
            return null;
        }
        User safetyUser = new User();
        safetyUser.setId(user.getId());
        safetyUser.setUserAccount(user.getUserAccount());
        safetyUser.setUsername(user.getUsername());
        safetyUser.setAvatarUrl(user.getAvatarUrl());
        safetyUser.setGender(user.getGender());
        safetyUser.setPhone(user.getPhone());
        safetyUser.setPlanetCode(user.getPlanetCode());
        safetyUser.setEmail(user.getEmail());
        safetyUser.setUserRole(user.getUserRole());
        safetyUser.setUserStatus(user.getUserStatus());
        safetyUser.setProfile(user.getProfile());
        safetyUser.setCreateTime(user.getCreateTime());
        safetyUser.setTags(user.getTags());
        return safetyUser;
    }

    @Override
    public User currentUser(HttpServletRequest request) {
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "当前用户未登录");
        }
        //因为我们往session中存的是脱敏后的user，如果想要获取用户全部信息，把注释打开即可，再从数据库通过id拿一遍user
//        Long id = currentUser.getId();
//        User user = this.getById(id);
        return getSafetyUser(currentUser);
    }

    @Override
    public Integer userLogout(HttpServletRequest request) {
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return 1;
    }

    /**
     * 用内存过滤的方式
     * @param tagNameList
     * @return
     */
    @Override
    public List<User> searchUsersByTags(List<String> tagNameList) {
        if (CollectionUtil.isEmpty(tagNameList)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        List<User> userList = this.list(queryWrapper);

        Gson gson = new Gson();
        //2.在内存中判断是否包含要求的标签(先查询所有的用户到内存中，在内存中做判断)
        return userList.stream().filter(user -> {
            String tagsJson = user.getTags();
            Set<String> tempTagNameSet = gson.fromJson(tagsJson, new TypeToken<Set<String>>() {
            }.getType());
            //下面这一行代码相当于判空，不用if是因为想尽量减少分支，减少代码复杂度
            //Optional可选类，java1.8新特性  ofNullable() 如果为括号里的值空，就给它一个orElse的默认值
            tempTagNameSet = Optional.ofNullable(tempTagNameSet).orElse(new HashSet<>());

            for (String tagName : tagNameList) {  //遍历我们传入的tagNameList
                //如果有一个标签不存在数据库中的tempTagNameSet，就失败
                if (!tempTagNameSet.contains(tagName)) {
                    return false;
                }
            }
            return true;
        }).map(this::getSafetyUser).collect(Collectors.toList());
    }

    @Override
    public Boolean updateUser(User user, HttpServletRequest request) {
        long userId = user.getId();
        if (userId < 1) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //todo 补充校验，如果用户没有传任何更新值，就直接报错

        //如果是管理员，允许修改所有的用户
        if (isAdmin(request)) {
            return this.updateById(user);
        }
        //如果不是管理员，判断修改的用户是不是当前登录的
        if (userId != currentUser(request).getId()) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        //更新
        return updateById(user);
    }

    @Override
    public Page<User> usersRecommend(long pageSize, long pageNum, HttpServletRequest request) {

        User user = this.currentUser(request);
        String redisKey = USER_RECOMMEND + user.getId();

        //查询redis中是否有缓存
        Page<User> userPage = (Page<User>) redisTemplate.opsForValue().get(USER_RECOMMEND);
        //不为空直接处理数据返回
        if (userPage != null) {
            return userPage;
        }
        //无缓存则查询数据库
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        userPage = this.page(new Page<>(pageNum, pageSize), queryWrapper);

        //更新redis中的缓存
        try {
            redisTemplate.opsForValue().set(USER_RECOMMEND,userPage,LOGIN_USER_TTL,TimeUnit.MINUTES);
        } catch (BusinessException e) {
            log.error("写入缓存失败");
        }
        return userPage;
    }

    @Deprecated // 用sql模糊查询的方式 拼接 and 查询 （sql）
    private List<User> searchUsersByTagsBySQL(List<String> tagNameList) {
        if (CollectionUtil.isEmpty(tagNameList)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        //like返回的就是一个queryWrapper ，因此可以重复添加like查询条件
        for (String tagName : tagNameList) {
            queryWrapper = queryWrapper.like("tags", tagName);
        }
        List<User> userList = this.list(queryWrapper);
        return userList.stream().map(this::getSafetyUser).collect(Collectors.toList());
    }

    /**
     * 判断用户是否是管理员
     *
     * @param request
     * @return
     */
    private boolean isAdmin(HttpServletRequest request) {
        Object userObject = request.getSession().getAttribute(USER_LOGIN_STATE);
        User user = (User) userObject;
        return user != null && user.getUserRole() == ADMIN_ROLE;
    }

    private boolean isAdmin(User user) {



        return user != null && user.getUserRole() == ADMIN_ROLE;
    }
}




