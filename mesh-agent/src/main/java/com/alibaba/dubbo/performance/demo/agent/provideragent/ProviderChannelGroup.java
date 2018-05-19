package com.alibaba.dubbo.performance.demo.agent.provideragent;

import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;

import java.util.concurrent.atomic.AtomicInteger;

public class ProviderChannelGroup {
    private ProviderChannel[] providerChannnels=null;
    private int channelNum;
    private AtomicInteger count=new AtomicInteger();

    public ProviderChannelGroup(int num, EventLoopGroup group){
        channelNum=num;
        providerChannnels=new ProviderChannel[num];
        try {
            for(int i=0;i<num;i++){
                providerChannnels[i]=new ProviderChannel();
                providerChannnels[i].setWorkerGroup(group);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public Channel nextChannel() throws Exception{
        return providerChannnels[count.getAndIncrement()%channelNum].getChannel();
    }
}
