package com.alibaba.dubbo.performance.demo.agent.ComsumerAgent;


import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpRequestEncoder;
import io.netty.handler.codec.http.HttpResponseDecoder;


public class RemoteInitializer extends ChannelInitializer<SocketChannel> {
    ChannelHandlerContext parentCtx;
    RemoteInitializer(ChannelHandlerContext ctx){
        parentCtx=ctx;
    }
    @Override
    protected void initChannel(SocketChannel socketChannel) {
        ChannelPipeline pipeline = socketChannel.pipeline();
        pipeline.addLast(new HttpResponseDecoder());
        pipeline.addLast(new HttpRequestEncoder());
        pipeline.addLast(new RemoteInboundHandler(parentCtx));
    }
}
