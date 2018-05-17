package com.alibaba.dubbo.performance.demo.agent.provideragent;


import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;

import io.netty.channel.*;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollDatagramChannel;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;


/**
 * Created by 79422 on 2018/5/4.
 */
public class ProviderChannelManager{

    private static  Channel channel=null;

    public static void initChannel(EventLoopGroup group) throws Exception{
        int port = Integer.valueOf(System.getProperty("dubbo.protocol.port"));
        //int port=20889;
        Class<? extends SocketChannel> channelClass= Epoll.isAvailable() ? EpollSocketChannel.class:NioSocketChannel.class;
        channel = new Bootstrap()
                .group(group)
                .channel(channelClass)
                //.group(new EpollEventLoopGroup())
                //.channel(EpollSocketChannel.class)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        ChannelPipeline pipeline = socketChannel.pipeline();
                        pipeline.addLast(new DubboRpcEncoder2());
                        pipeline.addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE,12,4,0,0));
                        //pipeline.addLast(new DubboRpcDecoder());
                        pipeline.addLast(new RpcMsgHandler3());
                    }
                })
                .connect("127.0.0.1", port).sync().channel();
    }


    public static Channel getChannel() throws Exception{
        return channel;
    }

}
