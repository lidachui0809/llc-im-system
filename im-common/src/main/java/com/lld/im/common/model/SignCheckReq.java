package com.lld.im.common.model;

import lombok.Data;

@Data
public class SignCheckReq {

    private String appId;
    private String userSign;
    private String userId;
}
