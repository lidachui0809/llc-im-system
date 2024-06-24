package com.lld.im.codec.pack.conversation;

import lombok.Data;

/**
 * @description:
 *
 * @version: 1.0
 */
@Data
public class UpdateConversationPack {

    private String conversationId;

    private Integer isMute;

    private Integer isTop;

    private Integer conversationType;

    private Long sequence;

}
