package com.lld.im.codec.pack.user;

import com.lld.im.common.model.UserSession;
import lombok.Data;
//import sun.dc.pr.PRError;

import java.util.List;

/**
 * @description:
 * 
 * @version: 1.0
 */
@Data
public class UserStatusChangeNotifyPack {

    private Integer appId;

    private String userId;

    private Integer status;

    /**
     * 用户的所有在线客户端
     */
    private List<UserSession> client;

}
