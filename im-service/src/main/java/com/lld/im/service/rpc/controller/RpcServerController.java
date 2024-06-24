package com.lld.im.service.rpc.controller;

import com.lld.im.common.BaseErrorCode;
import com.lld.im.common.ResponseVO;
import com.lld.im.common.config.AppConfig;
import com.lld.im.common.exception.ApplicationExceptionEnum;
import com.lld.im.common.model.SignCheckReq;
import com.lld.im.common.model.message.CheckSendMessageReq;
import com.lld.im.common.utils.SigAPI;
import com.lld.im.common.utils.SignCheckUtils;
import com.lld.im.service.message.service.P2PMessageService;
import com.lld.im.service.rpc.model.req.ZkUserInfoReq;
import com.lld.im.service.rpc.server.ZkRpcServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 负责远程服务调用 比如获得zk的注册信息
 */
@RestController
//@Hidden
@RequestMapping("/v1/rpc")
public class RpcServerController {

    @Autowired
    private ZkRpcServer zkRpcServer;
    @Autowired
    private AppConfig appConfig;

    @Autowired
    private P2PMessageService p2PMessageService;


    /**
     * 获得tcp网关层服务地址
     *
     * @param zkUserInfoReq
     * @return
     */

    @PostMapping("/zk/servers")
    public ResponseVO getZkServer(@RequestBody ZkUserInfoReq zkUserInfoReq) {
        return zkRpcServer.getZkServer(zkUserInfoReq);
    }

    @RequestMapping("/message/checkSend")
    public ResponseVO checkSend(@RequestBody @Validated CheckSendMessageReq req)  {
        return p2PMessageService.imServerPermissionCheck(req.getFromId(),req.getToId()
                ,req.getAppId());
    }

    @PostMapping("/user/checkSign")
    public ResponseVO checkSign(@RequestBody SignCheckReq req) {

        ApplicationExceptionEnum applicationExceptionEnum = SignCheckUtils.checkSign(req.getAppId(), req.getUserId()
                , appConfig.getPrivateKey(), req.getUserSign(), SigAPI.decodeUserSig(req.getUserSign()), req.getUserId());
        if (applicationExceptionEnum.getCode() == BaseErrorCode.SUCCESS.getCode()) {
            return ResponseVO.successResponse(applicationExceptionEnum);
        }
        return ResponseVO.errorResponse(applicationExceptionEnum);
    }

}
