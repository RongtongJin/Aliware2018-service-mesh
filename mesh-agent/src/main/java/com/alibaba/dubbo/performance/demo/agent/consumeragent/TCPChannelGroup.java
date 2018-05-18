package com.alibaba.dubbo.performance.demo.agent.consumeragent;

import com.alibaba.dubbo.performance.demo.agent.registry.Endpoint;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;

import java.util.concurrent.atomic.AtomicInteger;

public class TCPChannelGroup {
    private TCPChannel[] tcpChannels=null;
    private int channelNum;
    private AtomicInteger count=new AtomicInteger();

    public TCPChannelGroup (int num, EventLoopGroup group, Endpoint ep){
        channelNum=num;
        tcpChannels=new TCPChannel[num];
        try {
            for(int i=0;i<num;i++){
                tcpChannels[i]=new TCPChannel(group,ep);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public Channel nextChannel() throws Exception{
        return tcpChannels[count.getAndIncrement()%channelNum].getChannel();
    }
}
