package com.alibaba.dubbo.performance.demo.agent.provideragent.tcp;


import com.alibaba.dubbo.performance.demo.agent.registry.EtcdRegistry;
import com.alibaba.dubbo.performance.demo.agent.registry.IRegistry;
import com.alibaba.dubbo.performance.demo.agent.utils.ConstUtil;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.ServerChannel;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollChannelOption;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

public class TcpProviderAgent {

    private static Channel channel=null;

    private IRegistry registry = new EtcdRegistry(System.getProperty("etcd.url"));


    public void start(int port) throws Exception{

        boolean epollAvail=Epoll.isAvailable();
        EventLoopGroup workerGroup=null;
        if(epollAvail){
            workerGroup = new EpollEventLoopGroup(1);
        }else{
            workerGroup = new NioEventLoopGroup(1);
        }
        Class<? extends ServerChannel> channelClass = epollAvail ? EpollServerSocketChannel.class : NioServerSocketChannel.class;


        try {
            channel = new ServerBootstrap()
                    //.group(bossGroup,workerGroup)
                    .group(workerGroup,workerGroup)
                    .channel(channelClass)
                    .option(ChannelOption.RCVBUF_ALLOCATOR, ConstUtil.LARGE_BUF_ALLOCATOR)
                    .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childOption(ChannelOption.TCP_NODELAY,true)
                    .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                    .childOption(ChannelOption.RCVBUF_ALLOCATOR, ConstUtil.LARGE_BUF_ALLOCATOR)
                    .childOption(ChannelOption.ALLOW_HALF_CLOSURE, Boolean.FALSE)
                    .childOption(EpollChannelOption.TCP_QUICKACK,Boolean.TRUE)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            ChannelPipeline pipeline = socketChannel.pipeline();
                            pipeline.addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE,0,2,0,2));
//                            pipeline.addLast(new LengthBaseDecoder(1,0,2,2));
                            pipeline.addLast(new TcpConsumerAgentMsgHandler());
                        }
                    }).bind(port).sync().channel();
            System.out.println("TcpProviderAgent start on "+port);
            channel.closeFuture().await();
        }finally {
            workerGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws Exception{
        new TcpProviderAgent().start(30000);
    }
}
