package com.alibaba.dubbo.performance.demo.agent.provideragent.model;

import io.netty.buffer.ByteBuf;

public class RpcResponse {


    private long requestId;
    private ByteBuf buf;

    public RpcResponse(long requestId, ByteBuf buf) {
        this.requestId = requestId;
        this.buf = buf;
    }

    public long getRequestId() {
        return requestId;
    }

    public void setRequestId(long requestId) {
        this.requestId = requestId;
    }

    public ByteBuf getBuf() {
        return buf;
    }

    public void setBuf(ByteBuf buf) {
        this.buf = buf;
    }
}
