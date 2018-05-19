package com.alibaba.dubbo.performance.demo.agent.provideragent.model;

import io.netty.buffer.ByteBuf;

public class RpcRequest {
    private ByteBuf id;
    private ByteBuf parameter=null;

    public RpcRequest(ByteBuf id, ByteBuf parameter){
        this.id=id;
        this.parameter=parameter;
    }

    public ByteBuf  getId() {
        return id;
    }

    public ByteBuf getParameter() {
        return parameter;
    }
}
