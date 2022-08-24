package com.liva.usercenter.ScheduleJob;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.liva.usercenter.exception.BusinessException;
import com.liva.usercenter.model.domain.User;
import com.liva.usercenter.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.liva.usercenter.comment.Constant.*;

/**
 * 缓存预热任务
 * @author liva
 */
@Component
@Slf4j
public class PreCacheJob {

    @Resource
    private UserService userService;


    @Resource
    private RedisTemplate redisTemplate;

    //重点用户缓存
    private List<Long> mainUserList = Arrays.asList(1L);

    //每小时4分 执行一次
    //不用记，直接百度搜索crontab在线工具
    @Scheduled(cron = "0 11 0 * * *")
    public void doCatchRecommUser(){
        for (Long userId : mainUserList) {
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            Page<User> userPage = userService.page(new Page<>(1, 20), queryWrapper);

            String redisKey = USER_MAIN_RECOMMEND + userId;

            try {
                redisTemplate.opsForValue().set(redisKey,userPage,30,TimeUnit.MINUTES);
            } catch (Exception e) {
             log.error("redis set key error");
            }
        }
    }

}
