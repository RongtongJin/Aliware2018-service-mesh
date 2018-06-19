package com.alibaba.dubbo.performance.demo.agent.consumeragent.tcp;


import com.alibaba.dubbo.performance.demo.agent.registry.Endpoint;
import com.alibaba.dubbo.performance.demo.agent.utils.ConstUtil;
import com.alibaba.dubbo.performance.demo.agent.utils.EnumKey;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollChannelOption;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;


public final class TcpChannel {
    public Channel channel=null;
    public EnumKey type;
    public TcpChannel(EnumKey type, EventLoopGroup group, Endpoint endpoint) throws Exception{
        this.type=type;
        Class<? extends SocketChannel> channelClass=Epoll.isAvailable() ? EpollSocketChannel.class:NioSocketChannel.class;
        channel = new Bootstrap()
                .group(group)
                .channel(channelClass)
                .option(ChannelOption.SO_KEEPALIVE, Boolean.TRUE)
                .option(ChannelOption.TCP_NODELAY, Boolean.TRUE)
                .option(EpollChannelOption.TCP_QUICKACK,Boolean.TRUE)
                .option(ChannelOption.RCVBUF_ALLOCATOR, ConstUtil.SMALL_BUF_ALLOCATOR)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        ChannelPipeline pipeline = socketChannel.pipeline();
                        pipeline.addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE,0,1,0,1));
                        pipeline.addLast(new TcpProviderAgentMsgHandler());
                    }
                })
                .connect(endpoint.getHost(), endpoint.getPort()).sync().channel();
        System.out.println("connect to"+endpoint.getHost()+":"+endpoint.getPort());
    }
}
