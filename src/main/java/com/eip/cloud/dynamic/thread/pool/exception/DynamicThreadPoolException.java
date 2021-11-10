package com.eip.cloud.dynamic.thread.pool.exception;

/**
 * @Author: Barnett
 * @Date: 2021/11/8 17:53
 * @Description: 自定义异常
 */
public class DynamicThreadPoolException extends RuntimeException {

    private String message;

    public DynamicThreadPoolException(String message){
        super(message);
    }

}
