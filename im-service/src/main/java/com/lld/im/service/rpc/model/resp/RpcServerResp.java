package com.lld.im.service.rpc.model.resp;

import lombok.Data;

@Data
public class RpcServerResp {

    private Integer port;
    private String ip;
    private String bindHost;

    public RpcServerResp(Integer port, String ip, String bindHost) {
        this.port = port;
        this.ip = ip;
        this.bindHost = bindHost;
    }
}
