package com.itheima.common;

/**
 * ThreadLocal工具类
 */
public class BaseContext {
    private static  ThreadLocal<Long> threadLocal = new ThreadLocal<>();

    //设置ID到ThreadLocal中
    public static void set(Long id){
        threadLocal.set(id);
    }

    //从ThreadLocal中获取ID
    public static Long get(){
        return threadLocal.get();
    }
}
