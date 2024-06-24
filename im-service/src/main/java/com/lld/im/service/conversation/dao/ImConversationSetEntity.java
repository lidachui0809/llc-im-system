package com.lld.im.service.conversation.dao;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @author: Chackylee
 * @description:
 **/
@Data
@TableName("im_conversation_set")
public class ImConversationSetEntity {

    //会话id 0_fromId_toId
    private String conversationId;

    //会话类型 群聊/单聊
    private Integer conversationType;

    private String fromId;

    private String toId;

    //拓展字段 免打扰 以及置顶
    private int isMute;

    private int isTop;

    private Long sequence;

    //已读消息序列
    private Long readedSequence;

    private Integer appId;
}
