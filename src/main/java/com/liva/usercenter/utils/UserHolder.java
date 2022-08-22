package com.liva.usercenter.utils;

import com.liva.usercenter.model.domain.User;

public class UserHolder {

    private ThreadLocal<User> t1 = new ThreadLocal<User>();

    public User getUser(){
        return t1.get();
    }

    public void setUser(User user){
        t1.set(user);
    }

    public void removeUser(){
        t1.remove();
    }
}
