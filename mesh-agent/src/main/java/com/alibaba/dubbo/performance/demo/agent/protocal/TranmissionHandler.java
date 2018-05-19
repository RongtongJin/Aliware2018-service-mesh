package com.alibaba.dubbo.performance.demo.agent.protocal;

import com.alibaba.dubbo.performance.demo.agent.consumeragent.ChannelHolder;
import com.alibaba.dubbo.performance.demo.agent.consumeragent.TCPChannel;
import com.alibaba.dubbo.performance.demo.agent.registry.Endpoint;
import com.alibaba.dubbo.performance.demo.agent.utils.SimpleRegistryUtil;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public final class TranmissionHandler {
    public static List<Endpoint> endpoints = new ArrayList<>(4);
    public static List<TCPChannel> clients = new ArrayList<>(4);
    public static List<Channel> channels = new ArrayList<>(4);
    public static AtomicInteger channelId=new AtomicInteger(0);
    public static int bucket=1;

    public static boolean cacheEndpoints(String registryAddress) throws Exception {
        SimpleRegistryUtil.findProviders(registryAddress, endpoints);
        if (endpoints.size() == 0) {
            throw new Exception("provider agent endpoints==0");
        }
        return true;
    }
    public static void iniClients(EventLoopGroup workGroup) {
        for (int i = 0; i < endpoints.size(); i++) {
            clients.add(new TCPChannel(workGroup, endpoints.get(i)));
        }
    }

    public static void startClientsAndChannel() {
        for (int i = 0; i < clients.size(); i++) {
            try {
                channels.add(clients.get(i).getChannel());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        bucket=channels.size();
    }
    public static  int loadBlance(){
        if(bucket==1) return 0;
        else return channelId.getAndDecrement()%bucket;
    }
    public static int getClient() {
        return 0;
    }

    public static void forwarded(AgentHttpRequest msg) {
        channels.get(loadBlance()).writeAndFlush(msg.bodyBuf);
    }

    public static void main(String[] args) {
    }
}
