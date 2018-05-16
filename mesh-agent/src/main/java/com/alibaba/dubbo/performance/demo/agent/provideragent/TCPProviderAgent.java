package com.alibaba.dubbo.performance.demo.agent.provideragent;

import com.alibaba.dubbo.performance.demo.agent.consumeragent.TCPChannel;
import com.alibaba.dubbo.performance.demo.agent.consumeragent.TCPProviderAgentMsgHandler;
import com.alibaba.dubbo.performance.demo.agent.registry.EtcdRegistry;
import com.alibaba.dubbo.performance.demo.agent.registry.IRegistry;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
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
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.LineBasedFrameDecoder;

import java.net.InetSocketAddress;

public class TCPProviderAgent {

    private static Channel channel=null;

    private IRegistry registry = new EtcdRegistry(System.getProperty("etcd.url"));

    private static ProviderChannelGroup group=null;

    public static ProviderChannelGroup getChannelGroup(){
        return group;
    }

    public void start(int port) throws Exception{
        EventLoopGroup bossGroup=new NioEventLoopGroup(1);
        EventLoopGroup workGroup=new NioEventLoopGroup();

        //EventLoopGroup bossGroup=new EpollEventLoopGroup(1);
        //EventLoopGroup workGroup=new EpollEventLoopGroup();
        Thread.sleep(1000);

        group=new ProviderChannelGroup(13,workGroup);

        try {
            channel = new ServerBootstrap()
                    .group(bossGroup,workGroup)
                    .channel(NioServerSocketChannel.class)
                    //.channel(EpollServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG,128)
                    .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childOption(ChannelOption.TCP_NODELAY,true)
                    .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                    .childOption(ChannelOption.SO_REUSEADDR, Boolean.TRUE)
                    .childOption(ChannelOption.ALLOW_HALF_CLOSURE, Boolean.FALSE)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            ChannelPipeline pipeline = socketChannel.pipeline();
                            pipeline.addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE,0,2,0,2));
                            pipeline.addLast(new LengthFieldPrepender(2,false));
                            pipeline.addLast(new TCPConsumerAgentMsgHandler());
                        }
                    }).bind(port).sync().channel();
            System.out.println("TCPProviderAgent start on "+port);
            channel.closeFuture().await();
        }finally {
            bossGroup.shutdownGracefully();
            workGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws Exception{
        new TCPProviderAgent().start(30000);
    }
}
