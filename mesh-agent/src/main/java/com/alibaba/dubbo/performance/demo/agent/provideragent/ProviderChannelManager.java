package com.alibaba.dubbo.performance.demo.agent.provideragent;



import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollDatagramChannel;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;


/**
 * Created by 79422 on 2018/5/4.
 */
public class ProviderChannelManager{

    private static  Channel channel=null;

    public static void initChannel(EventLoopGroup group) throws Exception{
        int port = Integer.valueOf(System.getProperty("dubbo.protocol.port"));
        //int port=20889;
        channel = new Bootstrap()
                .group(group)
                .channel(NioSocketChannel.class)
                //.group(new EpollEventLoopGroup())
                //.channel(EpollSocketChannel.class)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.TCP_NODELAY, true)
                //.option(ChannelOption.ALLOCATOR, UnpooledByteBufAllocator.DEFAULT)
                .handler(new RpcHandlerInitializer())
                .connect("127.0.0.1", port).sync().channel();
    }


    public static Channel getChannel() throws Exception{
        return channel;
    }

}
