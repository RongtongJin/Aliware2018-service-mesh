package com.alibaba.dubbo.performance.demo.agent.ConsumerAgentTest;


import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.*;

public class RemoteInitializerTest extends ChannelInitializer<SocketChannel> {
    Channel parentChannel;
    RemoteInitializerTest(Channel cl){
        parentChannel=cl;
    }
    @Override
    protected void initChannel(SocketChannel socketChannel) {
        ChannelPipeline pipeline = socketChannel.pipeline();
        pipeline.addLast(new HttpResponseDecoder());
        pipeline.addLast(new HttpRequestEncoder());
        //pipeline.addLast(new HttpClientCodec());
        pipeline.addLast(new HttpObjectAggregator(1024));
        pipeline.addLast(new RemoteInboundHandlerTest(parentChannel));
    }
}