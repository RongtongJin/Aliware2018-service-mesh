package com.alibaba.dubbo.performance.demo.agent.ConsumerAgentTest;


import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.nio.NioDatagramChannel;


//fix me:这个类是可以省略的
public class UDPChannelInitialzer extends ChannelInitializer<NioDatagramChannel>{
    public UDPChannelInitialzer() {
        super();
    }

    @Override
    protected void initChannel(NioDatagramChannel ch) throws Exception {
        ChannelPipeline pipeline=ch.pipeline();
        pipeline.addLast(new ProviderAgentMsgHandler());
    }
}
