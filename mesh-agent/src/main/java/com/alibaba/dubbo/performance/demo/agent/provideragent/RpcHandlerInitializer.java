package com.alibaba.dubbo.performance.demo.agent.provideragent;


import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.socket.SocketChannel;

/**
 * Created by 79422 on 2018/5/4.
 */
public class RpcHandlerInitializer extends ChannelInitializer<SocketChannel> {

        @Override
        protected void initChannel(SocketChannel socketChannel) {
            ChannelPipeline pipeline = socketChannel.pipeline();
            pipeline.addLast(new DubboRpcEncoder());
            pipeline.addLast(new DubboRpcDecoder());
            pipeline.addLast(new RpcMsgHandler());
        }
}
