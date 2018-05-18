package com.alibaba.dubbo.performance.demo.agent.protocal;

import com.alibaba.dubbo.performance.demo.agent.registry.Endpoint;

import java.util.List;

public final class TramissionHandler {
    public static  List<Endpoint> endpoints=null;
    public static void iniClients(List<Endpoint> endpoints) {
        TramissionHandler.endpoints=endpoints;
        for(int i=0;i<endpoints.size();i++){

        }
    }
    public static  int getClient(){
        return 0;
    }
    public static void send(){

    }
}
