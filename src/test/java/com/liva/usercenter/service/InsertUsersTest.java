package com.liva.usercenter.service;


import com.liva.usercenter.mapper.UserMapper;
import com.liva.usercenter.model.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.annotation.Scheduled;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@SpringBootTest
public class InsertUsersTest {

    @Resource
    private UserService userService;

    /**
     * 批量插入用户
     */
//    @Test
//    public void doInsertUsers(){
//        final int INSERT_NUM = 100000;
//
//        List<User> userList = new ArrayList<>();
//
//        for (int i = 0; i < INSERT_NUM; i++) {
//
//            User user = new User();
//
//            user.setUserAccount("fakeUser");
//            user.setUsername("SMART莱欧"+i);
//            user.setAvatarUrl("https://pics2.baidu.com/feed/359b033b5bb5c9ea945a623ae338fa083bf3b343.jpeg?token=a017c50c19d973da8645a985fed42361");
//            user.setProfile("SMART_BRAIN_COMPANY_RIDER_READY!");
//            user.setGender(0);
//            user.setUserPassword("123456789");
//            user.setPhone("00-2321-1122");
//            user.setEmail("smartbraim@smart.com");
//            user.setCreateTime(new Date());
//            user.setUpdateTime(new Date());
//            user.setPlanetCode("1112"+i);
//            user.setTags("[]");
//            userList.add(user);
//        }
//        userService.saveBatch(userList,10000);
//    }

    /**
     * 并发批量插入
     */
    @Test
    public void doConcurrencyInsertUsers(){
        final int INSERT_NUM = 100000;

        int j = 0;
        List<CompletableFuture<Void>> futureList = new ArrayList<>();

        //分十组
        for (int i = 0; i < 50; i++) {
            List<User> userList = new ArrayList<>();
            while (true) {
                j++;
                User user = new User();
                user.setUserAccount("fakeUser");
                user.setUsername("SMART莱欧"+i);
                user.setAvatarUrl("https://pics2.baidu.com/feed/359b033b5bb5c9ea945a623ae338fa083bf3b343.jpeg?token=a017c50c19d973da8645a985fed42361");
                user.setProfile("SMART_BRAIN_COMPANY_RIDER_READY!");
                user.setGender(0);
                user.setUserPassword("123456789");
                user.setPhone("00-2321-1122");
                user.setEmail("smartbraim@smart.com");
                user.setCreateTime(new Date());
                user.setUpdateTime(new Date());
                user.setPlanetCode("1112"+i);
                user.setTags("[]");
                userList.add(user);
                if (j % 1000 == 0){
                    break;
                }
            }
            //异步执行
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                userService.saveBatch(userList, 10000);
            });
            futureList.add(future);
        }
        CompletableFuture.allOf(futureList.toArray(new CompletableFuture[]{})).join();
    }
}

