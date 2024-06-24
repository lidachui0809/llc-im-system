package com.lld.im.common.utils;

import com.alibaba.fastjson.JSONObject;
import com.lld.im.common.BaseErrorCode;
import com.lld.im.common.enums.GateWayErrorCode;
import com.lld.im.common.exception.ApplicationExceptionEnum;

import java.util.Objects;

public class SignCheckUtils {


    public static ApplicationExceptionEnum checkSign(String appId,String userId,String privateKey,String userSign,
                                                     JSONObject jsonObject, String identifier){
        //揭秘结果比对
        String encodeIdentifier = jsonObject.getString("TLS.identifier");
        String encodeAppId = jsonObject.getString("TLS.appId");
        Long encodeExpire = Long.valueOf(jsonObject.getString("TLS.expire"));
        Long encodeExpireTime = Long.valueOf(jsonObject.getString("TLS.expireTime"));
        //签名不一致
        if(!Objects.equals(identifier,encodeIdentifier)){
            return GateWayErrorCode.USERSIGN_OPERATE_NOT_MATE;
        }
        if(!Objects.equals(appId,encodeAppId)){
            return GateWayErrorCode.USERSIGN_IS_ERROR;
        }
        if(encodeExpire<=0L){
            return GateWayErrorCode.USERSIGN_IS_EXPIRED;
        }
        //判断过期 [单位是秒]
        if((encodeExpireTime+encodeExpire)<(System.currentTimeMillis()/1000)){
            return GateWayErrorCode.USERSIGN_IS_EXPIRED;
        }
        //再次加密
        //TODO 有概率出现前后加密不一致的情况
//        String reUserSig
//                = SigAPI.getUserSig(userId, privateKey, Long.parseLong(appId));
//        if(Objects.equals(reUserSig, userSign)){
//            return GateWayErrorCode.USERSIGN_IS_ERROR;
//        }
        return BaseErrorCode.SUCCESS;
    }


}
