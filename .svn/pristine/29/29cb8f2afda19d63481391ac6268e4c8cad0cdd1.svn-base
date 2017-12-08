package com.hst.mininurse.utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Author:lsh
 * Version: 1.0
 * Description:
 * Date: 2017/9/29
 */

public class ThreadPool {


      static ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(0, Integer.MAX_VALUE, 60L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>());
    public static ExecutorService getThreadPool(){
        return threadPoolExecutor;
    }


}
