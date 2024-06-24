package com.lld.im.service.user.model.resp;

import com.lld.im.service.rpc.model.resp.RpcServerResp;
import lombok.Data;

@Data
public class UserLoginResp {

    // 用户id
    private String userId;

    // 用户名称
    private String nickName;

    //位置
    private String location;

    //生日
    private String birthDay;

    // 头像
    private String photo;

    // 性别
    private Integer userSex;

    private String sign;
    private Integer clientType;
    private Integer appId;

    private RpcServerResp rpcServerResp;

}
