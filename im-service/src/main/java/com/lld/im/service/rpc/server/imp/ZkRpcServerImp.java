package com.lld.im.service.rpc.server.imp;

import com.lld.im.common.ResponseVO;
//import com.lld.im.common.router.RouterHandler;
import com.lld.im.common.route.RouteHandle;
import com.lld.im.service.rpc.model.req.ZkUserInfoReq;
import com.lld.im.service.rpc.model.resp.RpcServerResp;
import com.lld.im.service.rpc.server.ZkRpcServer;
import com.lld.im.service.utils.ZKit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 获得im连接到地址信息
 */
@Service
public class ZkRpcServerImp implements ZkRpcServer {

    @Autowired
    private RouteHandle routerHandler;

    @Autowired
    private ZKit zkKit;
    @Override
    public ResponseVO getZkServer(ZkUserInfoReq zkUserInfoReq) {
        List<String> servers = zkKit.getServers(zkUserInfoReq.getClientType());
        String server = routerHandler.getRouteServer(servers, zkUserInfoReq.getUserId());
        String[] split = server.split(":");
        RpcServerResp rpcServerResp = new RpcServerResp(Integer.parseInt(split[1]),split[0],server);
        return ResponseVO.successResponse(rpcServerResp);
    }
}
