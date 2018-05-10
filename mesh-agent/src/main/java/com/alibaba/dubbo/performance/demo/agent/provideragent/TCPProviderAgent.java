package com.alibaba.dubbo.performance.demo.agent.provideragent;

import com.alibaba.dubbo.performance.demo.agent.consumeragent.TCPProviderAgentMsgHandler;
import com.alibaba.dubbo.performance.demo.agent.registry.EtcdRegistry;
import com.alibaba.dubbo.performance.demo.agent.registry.IRegistry;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;

import java.net.InetSocketAddress;

public class TCPProviderAgent {

    private static Channel channel=null;

    private IRegistry registry = new EtcdRegistry(System.getProperty("etcd.url"));

    public void start(int port) throws Exception{
        //EventLoopGroup eventLoopGroup=new NioEventLoopGroup();

        EventLoopGroup eventLoopGroup=new EpollEventLoopGroup();

        Thread.sleep(1000);
        //ProviderChannelManager.initChannel(eventLoopGroup);

        try {
            channel = new ServerBootstrap()
                    .group(eventLoopGroup,eventLoopGroup)
                    //.channel(NioServerSocketChannel.class)
                    .channel(EpollServerSocketChannel.class)
                    //.option(ChannelOption.SO_KEEPALIVE, true)
                    //.option(ChannelOption.TCP_NODELAY, true)
                    .childOption(ChannelOption.SO_KEEPALIVE,true)
                    .childOption(ChannelOption.TCP_NODELAY,true)
                    //.option(ChannelOption.ALLOCATOR, UnpooledByteBufAllocator.DEFAULT)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            ChannelPipeline pipeline = socketChannel.pipeline();
                            ByteBuf delimiter = Unpooled.wrappedBuffer("$".getBytes());
                            pipeline.addLast(new DelimiterBasedFrameDecoder(1024,delimiter));
                            pipeline.addLast(new TCPConsumerAgentMsgHandler());
                        }
                    }).bind(port).sync().channel();
            System.out.println("TCPProviderAgent start on "+port);
            channel.closeFuture().await();
        }finally {
            eventLoopGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws Exception{
        new TCPProviderAgent().start(30000);
    }
}
