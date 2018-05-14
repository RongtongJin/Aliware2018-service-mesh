package com.alibaba.dubbo.performance.demo.agent.provideragent.model;

import io.netty.buffer.ByteBuf;

public class RpcRequest {
    private long id;
    private byte[] parameter=null;

    public RpcRequest(long id, byte[] parameter){
        this.id=id;
        this.parameter=parameter;
    }


    public long getId() {
        return id;
    }

    public byte[] getParameter() {
        return parameter;
    }
}
