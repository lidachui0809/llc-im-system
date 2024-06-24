package com.lld.im.tcp.feign;

import com.lld.im.common.ResponseVO;
import com.lld.im.common.model.SignCheckReq;
import com.lld.im.common.model.message.CheckSendMessageReq;
import feign.Headers;
import feign.RequestLine;

/**
 * @description:
 * 
 * @version: 1.0
 */
public interface FeignMessageRpcService {

    @Headers({"Content-Type: application/json","Accept: application/json"})
    @RequestLine("POST /rpc/message/checkSend")
    public ResponseVO checkSendMessage(CheckSendMessageReq o);

    @Headers({"Content-Type: application/json","Accept: application/json"})
    @RequestLine("POST /rpc/user/checkSign")
    public ResponseVO checkSign(SignCheckReq req);
}
