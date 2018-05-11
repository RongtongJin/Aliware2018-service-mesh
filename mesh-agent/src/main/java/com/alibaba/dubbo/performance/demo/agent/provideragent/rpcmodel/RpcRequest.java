package com.alibaba.dubbo.performance.demo.agent.provideragent.rpcmodel;

public class RpcRequest {
    private long id;
    private String parameter=null;

    public RpcRequest(long id, String parameter){
        this.id=id;
        this.parameter=parameter;
    }


    public long getId() {
        return id;
    }

    public String getParameter() {
        return parameter;
    }
}
