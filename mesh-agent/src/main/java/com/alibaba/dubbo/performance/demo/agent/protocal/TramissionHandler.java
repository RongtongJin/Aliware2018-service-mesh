package com.alibaba.dubbo.performance.demo.agent.protocal;

import com.alibaba.dubbo.performance.demo.agent.consumeragent.TCPChannel;
import com.alibaba.dubbo.performance.demo.agent.registry.Endpoint;
import com.alibaba.dubbo.performance.demo.agent.utils.SimpleRegistryUtil;
import io.netty.channel.EventLoopGroup;

import java.util.ArrayList;
import java.util.List;

public final class TramissionHandler {
    public static  List<Endpoint> endpoints=new ArrayList<>(4);
    public static  List<TCPChannel> clients=new ArrayList<>(4);
    public static  boolean cacheEndpoints(String registryAddress) throws Exception {
        SimpleRegistryUtil.findProviders(registryAddress,endpoints);
        if(endpoints.size()==0){
            throw new Exception("provider agent endpoints==0");
        }
        return true;
    }

    public static void iniClients(EventLoopGroup workGroup) {
        for(int i=0;i<endpoints.size();i++){
            clients.add(new TCPChannel(workGroup,endpoints.get(i)));
        }
    }
    public static void startClients() {
        for(int i=0;i<clients.size();i++){
            try {
                clients.get(i).getChannel();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
    public static  int getClient(){
        return 0;
    }
    public static void send(){

    }

    public static void main(String[] args) {
    }
}
