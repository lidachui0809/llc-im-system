package com.lld.im.service.interceptor;


import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.lld.im.common.BaseErrorCode;
import com.lld.im.common.config.AppConfig;
import com.lld.im.common.constant.Constants;

import com.lld.im.common.enums.GateWayErrorCode;
import com.lld.im.common.exception.ApplicationExceptionEnum;
import com.lld.im.common.utils.SigAPI;
import com.lld.im.common.utils.SignCheckUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Component
public class SignCheck {

//    @Autowired
//    private AppConfig appConfig;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private AppConfig appConfig;

    public ApplicationExceptionEnum checkSign(String appId, String userId, String userSign, String identifier){
        String key=appId+ Constants.RedisConstants.UserSessionConstants+identifier;
        String signExpTime = stringRedisTemplate.opsForValue().get(key);
        if(signExpTime!=null&&Long.parseLong(signExpTime)>(System.currentTimeMillis()/1000)){
            return BaseErrorCode.SUCCESS;
        }
        //对于用户签名进行解密
        JSONObject jsonObject = SigAPI.decodeUserSig(userSign);
        ApplicationExceptionEnum applicationExceptionEnum
                = SignCheckUtils.checkSign(appId,userId,appConfig.getPrivateKey(),userSign ,jsonObject, identifier);
        if(applicationExceptionEnum!=BaseErrorCode.SUCCESS){
            return applicationExceptionEnum;
        }
        Long encodeExpire = Long.valueOf(jsonObject.getString("TLS.expire"));
        Long encodeExpireTime = Long.valueOf(jsonObject.getString("TLS.expireTime"));
        //将结果存入redis 将过期事件存入redis
        stringRedisTemplate.opsForValue().set(key,encodeExpireTime.toString(),encodeExpire, TimeUnit.SECONDS);
        return BaseErrorCode.SUCCESS;
    }

}
