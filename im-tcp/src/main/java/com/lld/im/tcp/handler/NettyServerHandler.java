package com.lld.im.tcp.handler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.lld.im.codec.pack.LoginPack;
import com.lld.im.codec.pack.message.ChatMessageAck;
import com.lld.im.codec.pack.user.LoginAckPack;
import com.lld.im.codec.pack.user.UserStatusChangeNotifyPack;
import com.lld.im.codec.proto.Message;
import com.lld.im.codec.proto.MessagePack;
import com.lld.im.common.ResponseVO;
import com.lld.im.common.constant.Constants;
import com.lld.im.common.enums.ImConnectStatusEnum;
import com.lld.im.common.enums.command.GroupEventCommand;
import com.lld.im.common.enums.command.MessageCommand;
import com.lld.im.common.enums.command.SystemCommand;
import com.lld.im.common.enums.command.UserEventCommand;
import com.lld.im.common.model.UserClientDto;
import com.lld.im.common.model.UserSession;
import com.lld.im.common.model.message.CheckSendMessageReq;
import com.lld.im.tcp.feign.FeignMessageRpcService;
import com.lld.im.tcp.publish.MqMessageProducer;
import com.lld.im.tcp.redis.RedisManager;
import com.lld.im.tcp.utils.SessionSocketHolder;
import feign.Feign;
import feign.Request;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.AttributeKey;
import org.redisson.api.RMap;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;

/**
 * @description:
 * 
 * @version: 1.0
 */
public class NettyServerHandler extends SimpleChannelInboundHandler<Message> {


    private Integer brokerId;


    private FeignMessageRpcService feignMessageRpcService;


    public NettyServerHandler(Integer brokerId, FeignMessageRpcService rpcService) {
        this.brokerId = brokerId;
        this.feignMessageRpcService = rpcService;
    }

    //String
    //Map
    // userId client1 session
    // userId client2 session
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) throws Exception {

        Integer command = msg.getMessageHeader().getCommand();
        //登录command
        if (command == SystemCommand.LOGIN.getCommand()) {
            doLogin(ctx, msg);
        } else if (command == SystemCommand.LOGOUT.getCommand()) {
            //删除session
            //redis 删除
            SessionSocketHolder.removeUserSession((NioSocketChannel) ctx.channel());
        } else if (command == SystemCommand.PING.getCommand()) {
            //心跳检测
            ctx.channel()
                    .attr(AttributeKey.valueOf(Constants.ReadTime)).set(System.currentTimeMillis());
        } else if (command == MessageCommand.MSG_P2P.getCommand()
                || command == GroupEventCommand.MSG_GROUP.getCommand()) {
            doChartMsgSend(ctx, msg, command);
        } else {
            MqMessageProducer.sendMessage(msg, command);
        }

    }

    private void doChartMsgSend(ChannelHandlerContext ctx, Message msg, Integer command) {
        try {
            //TODO 前置校验 使用feign 远程调用server层
            String toId = "";
            CheckSendMessageReq req = new CheckSendMessageReq();
            req.setAppId(msg.getMessageHeader().getAppId());
            req.setCommand(msg.getMessageHeader().getCommand());
            JSONObject jsonObject = JSON.parseObject(JSONObject.toJSONString(msg.getMessagePack()));
            String fromId = jsonObject.getString("fromId");
            if (command == MessageCommand.MSG_P2P.getCommand()) {
                toId = jsonObject.getString("toId");
            } else {
                toId = jsonObject.getString("groupId");
            }
            req.setToId(toId);
            req.setFromId(fromId);

            ResponseVO responseVO = feignMessageRpcService.checkSendMessage(req);
            if (responseVO.isOk()) {
                MqMessageProducer.sendMessage(msg, command);
            } else {
                Integer ackCommand = 0;
                if (command == MessageCommand.MSG_P2P.getCommand()) {
                    ackCommand = MessageCommand.MSG_ACK.getCommand();
                } else {
                    ackCommand = GroupEventCommand.GROUP_MSG_ACK.getCommand();
                }

                ChatMessageAck chatMessageAck = new ChatMessageAck(jsonObject.getString("messageId"));
                responseVO.setData(chatMessageAck);
                MessagePack<ResponseVO> ack = new MessagePack<>();
                ack.setData(responseVO);
                ack.setCommand(ackCommand);
                ctx.channel().writeAndFlush(ack);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void doLogin(ChannelHandlerContext ctx, Message msg) {
        LoginPack loginPack = getLoginPackToChannel(ctx, msg);
        //将channel存起来
        saveUserSessionToRedis(ctx, msg, loginPack);
        //发送用户登陆广播
        sendUserLoginBroadcast(msg, loginPack);

        sendUserOnlineMsg(msg, loginPack);
        //发送登录成功消息
        sendLoginSuccessMsg(ctx, msg, loginPack);
    }

    private void sendLoginSuccessMsg(ChannelHandlerContext ctx, Message msg, LoginPack loginPack) {
        MessagePack<LoginAckPack> loginSuccess = new MessagePack<>();
        LoginAckPack loginAckPack = new LoginAckPack();
        loginAckPack.setUserId(loginPack.getUserId());
        loginSuccess.setCommand(SystemCommand.LOGINACK.getCommand());
        loginSuccess.setData(loginAckPack);
        loginSuccess.setImei(msg.getMessageHeader().getImei());
        loginSuccess.setAppId(msg.getMessageHeader().getAppId());
        //直接回复给客户端
        ctx.channel().writeAndFlush(loginSuccess);
    }

    /**
     * 发送用户上线 到逻辑层
     * @param msg
     * @param loginPack
     */
    private void sendUserOnlineMsg(Message msg, LoginPack loginPack) {
        UserStatusChangeNotifyPack userStatusChangeNotifyPack = new UserStatusChangeNotifyPack();
        userStatusChangeNotifyPack.setAppId(msg.getMessageHeader().getAppId());
        userStatusChangeNotifyPack.setUserId(loginPack.getUserId());
        userStatusChangeNotifyPack.setStatus(ImConnectStatusEnum.ONLINE_STATUS.getCode());
        MqMessageProducer.sendMessage(userStatusChangeNotifyPack, msg.getMessageHeader(), UserEventCommand.USER_ONLINE_STATUS_CHANGE.getCommand());
    }

    private void saveUserSessionToRedis(ChannelHandlerContext ctx, Message msg, LoginPack loginPack) {
        UserSession userSession = new UserSession();
        userSession.setAppId(msg.getMessageHeader().getAppId());
        userSession.setClientType(msg.getMessageHeader().getClientType());
        userSession.setUserId(loginPack.getUserId());
        userSession.setConnectState(ImConnectStatusEnum.ONLINE_STATUS.getCode());
        userSession.setBrokerId(brokerId);
        userSession.setImei(msg.getMessageHeader().getImei());
        try {
            InetAddress localHost = InetAddress.getLocalHost();
            userSession.setBrokerHost(localHost.getHostAddress());
        } catch (Exception e) {
            e.printStackTrace();
        }

        RedissonClient redissonClient = RedisManager.getRedissonClient();
        RMap<String, String> map = redissonClient.getMap(msg.getMessageHeader().getAppId() + Constants.RedisConstants.UserSessionConstants + loginPack.getUserId());
        map.put(msg.getMessageHeader().getClientType() + ":" + msg.getMessageHeader().getImei()
                , JSONObject.toJSONString(userSession));
        SessionSocketHolder
                .put(msg.getMessageHeader().getAppId()
                        , loginPack.getUserId(),
                        msg.getMessageHeader().getClientType(), msg.getMessageHeader().getImei(), (NioSocketChannel) ctx.channel());
    }

    private LoginPack getLoginPackToChannel(ChannelHandlerContext ctx, Message msg) {
        LoginPack loginPack = JSON.parseObject(JSONObject.toJSONString(msg.getMessagePack()),
                new TypeReference<LoginPack>() {
                }.getType());
        /** 登陸事件 **/
        String userId = loginPack.getUserId();
        /** 为channel设置用户id **/
        ctx.channel().attr(AttributeKey.valueOf(Constants.UserId)).set(userId);
        String clientImei = msg.getMessageHeader().getClientType() + ":" + msg.getMessageHeader().getImei();
        /** 为channel设置client和imel **/
        ctx.channel().attr(AttributeKey.valueOf(Constants.ClientImei)).set(clientImei);
        /** 为channel设置appId **/
        ctx.channel().attr(AttributeKey.valueOf(Constants.AppId)).set(msg.getMessageHeader().getAppId());
        /** 为channel设置ClientType **/
        ctx.channel().attr(AttributeKey.valueOf(Constants.ClientType))
                .set(msg.getMessageHeader().getClientType());
        /** 为channel设置Imei **/
        ctx.channel().attr(AttributeKey.valueOf(Constants.Imei))
                .set(msg.getMessageHeader().getImei());
        return loginPack;
    }

    private void sendUserLoginBroadcast(Message msg, LoginPack loginPack) {
        UserClientDto dto = new UserClientDto();
        dto.setImei(msg.getMessageHeader().getImei());
        dto.setUserId(loginPack.getUserId());
        dto.setClientType(msg.getMessageHeader().getClientType());
        dto.setAppId(msg.getMessageHeader().getAppId());
        RTopic topic = RedisManager.getRedissonClient().getTopic(Constants.RedisConstants.UserLoginChannel);
        topic.publish(JSONObject.toJSONString(dto));
    }

    //表示 channel 处于不活动状态
//    @Override
//    public void channelInactive(ChannelHandlerContext ctx) {
//        //设置离线
//        SessionSocketHolder.offlineUserSession((NioSocketChannel) ctx.channel());
//        ctx.close();
//    }


    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {


    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        super.exceptionCaught(ctx, cause);

    }
}
