package com.lld.im.tcp.handler;

import com.alibaba.fastjson.JSONObject;
//import com.ecode.imecode.proto.Message;
//import com.ecode.imecode.proto.MessageHeader;
//import com.im.tcp.feign.RpcServer;
//import com.im.tcp.utils.SessionSocketHolder;
import com.lld.im.codec.proto.Message;
import com.lld.im.codec.proto.MessageHeader;
import com.lld.im.common.ResponseVO;
import com.lld.im.common.enums.GateWayErrorCode;
import com.lld.im.common.model.SignCheckReq;
import com.lld.im.tcp.feign.FeignMessageRpcService;
import com.lld.im.tcp.utils.SessionSocketHolder;
//import com.rabbitmq.client.RpcServer;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * 鉴权 判断用户是否登陆过
 */
@Slf4j
public class AuthorCheckHandler extends SimpleChannelInboundHandler<Message> {

    private FeignMessageRpcService rpcServer;

    public AuthorCheckHandler(FeignMessageRpcService rpcServer) {
        this.rpcServer=rpcServer;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Message msg) throws Exception {
        JSONObject bodyData = (JSONObject) msg.getMessagePack();
        MessageHeader messageHeader = msg.getMessageHeader();
        String userId=bodyData.getString("fromId");
        //判断用户是否已经连接过
        NioSocketChannel channel = SessionSocketHolder.get(messageHeader.getAppId(), userId, Integer.valueOf(String.valueOf(messageHeader.getClientType()))
                , msg.getMessageHeader().getImei());
        if(channel!=null){
            //直接投递消息
            channelHandlerContext.fireChannelRead(msg);
            return;
        }
        String sign = bodyData.getString("userSign");
        if(StringUtils.isBlank(sign)){
            log.error("im 通信层签名不通过拦截 msg=={}",msg);
            //写完之后 断开连接
            channelHandlerContext.channel()
                    .writeAndFlush(JSONObject.toJSONString(ResponseVO.errorResponse(GateWayErrorCode.USERSIGN_NOT_EXIST)))
                    .addListener(ChannelFutureListener.CLOSE);
            return;
        }
        SignCheckReq signCheckReq = new SignCheckReq();
        signCheckReq.setUserSign(sign);
        signCheckReq.setAppId(messageHeader.getAppId().toString());
        signCheckReq.setUserId(userId);
        //TODO feign远程调用 判断用户是否以及登录
        ResponseVO responseVO = rpcServer.checkSign(signCheckReq);
        if(!responseVO.isOk()){
            channelHandlerContext.channel().writeAndFlush(
                    JSONObject.toJSONString(responseVO)).addListener(ChannelFutureListener.CLOSE);
            log.error("鉴权失败！msg==={}",responseVO.getMsg());
            return;
        }
        log.info("鉴权成功！");
        channelHandlerContext.fireChannelRead(msg);
    }
}
