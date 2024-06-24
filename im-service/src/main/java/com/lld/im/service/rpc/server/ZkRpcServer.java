package com.lld.im.service.rpc.server;

import com.lld.im.common.ResponseVO;
import com.lld.im.service.rpc.model.req.ZkUserInfoReq;

public interface ZkRpcServer {

    ResponseVO getZkServer(ZkUserInfoReq zkUserInfoReq);
}
