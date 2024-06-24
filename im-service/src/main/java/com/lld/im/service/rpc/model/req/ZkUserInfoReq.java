package com.lld.im.service.rpc.model.req;

//import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
//@Schema
public class ZkUserInfoReq {

    private String userId;
//    @Schema(description = "客户端类型")
    private Integer clientType;
}
