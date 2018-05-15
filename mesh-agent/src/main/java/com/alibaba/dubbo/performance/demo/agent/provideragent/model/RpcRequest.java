package com.alibaba.dubbo.performance.demo.agent.provideragent.model;

import io.netty.buffer.ByteBuf;

public class RpcRequest {
    private long id;
    private ByteBuf parameter=null;

    public RpcRequest(long id, ByteBuf parameter){
        this.id=id;
        this.parameter=parameter;
    }

    public long getId() {
        return id;
    }

    public ByteBuf getParameter() {
        return parameter;
    }
}
