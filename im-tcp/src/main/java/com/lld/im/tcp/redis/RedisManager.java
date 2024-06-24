package com.lld.im.tcp.redis;

import com.lld.im.codec.config.BootstrapConfig;
import com.lld.im.tcp.reciver.UserLoginMessageListener;

import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;

/**
 * @description:
 * 
 * @version: 1.0
 */
@Slf4j
public class RedisManager {

    private static RedissonClient redissonClient;

    private static Integer loginModel;

    public static void init(BootstrapConfig config){
        loginModel = config.getLim().getLoginModel();
        SingleClientStrategy singleClientStrategy = new SingleClientStrategy();
        redissonClient = singleClientStrategy.getRedissonClient(config.getLim().getRedis());
        UserLoginMessageListener userLoginMessageListener = new UserLoginMessageListener(loginModel);
        userLoginMessageListener.listenerUserLogin();
        log.info("redis初始化完成----");
    }

    public static RedissonClient getRedissonClient(){
        return redissonClient;
    }

}
