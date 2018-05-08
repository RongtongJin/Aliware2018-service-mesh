package com.alibaba.dubbo.performance.demo.agent.provideragent.rpcmodel;

public class RpcRequest {
    private long id;
    private byte[] data=null;

    public RpcRequest(long id, byte[] data){
        this.id=id;
        this.data=data;
    }


    public long getId() {
        return id;
    }

    public byte[] getData() {
        return data;
    }
}
