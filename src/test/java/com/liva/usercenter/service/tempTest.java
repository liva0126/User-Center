package com.liva.usercenter.service;

import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Hashtable;
import java.util.concurrent.*;

@SpringBootTest
public class tempTest {

    @Test
    public static void test(){

        Executors.newFixedThreadPool(1);

        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(5
                , 5
                , 3000
                , TimeUnit.MILLISECONDS
                , new ArrayBlockingQueue<Runnable>(10)
                , Executors.defaultThreadFactory()
                , new ThreadPoolExecutor.CallerRunsPolicy());


    }
}

