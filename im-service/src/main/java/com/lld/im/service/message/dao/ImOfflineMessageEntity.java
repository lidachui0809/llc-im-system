package com.lld.im.service.message.dao;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("im_offline_message_pull_rec")
//离线消息表
public class ImOfflineMessageEntity {

    private Integer appId;

    /** messageBodyId*/
    private Long messageKey;

    private String userId;

    /**
     * 标识消息序列
     */
    private Long sequence;

//    /** messageBody*/
//    private String messageBody;
//
//    private String securityKey;
//
//    private Long messageTime;
//
//    private Long createTime;
//
//    private String extra;

//    private Integer delFlag;
}
