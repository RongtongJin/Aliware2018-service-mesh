package com.alibaba.dubbo.performance.demo.agent.provideragent.tcp;

import com.alibaba.dubbo.performance.demo.agent.provideragent.ProviderChannelManager;
import com.alibaba.dubbo.performance.demo.agent.provideragent.TCPConsumerAgentMsgHandler;
import com.alibaba.dubbo.performance.demo.agent.utils.SimpleRegistryUtil;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;

public class TCPProviderAgent {
    private static Channel channel=null;

    public void start(int port) throws Exception{
        EventLoopGroup bossGroup=new NioEventLoopGroup(1);
        EventLoopGroup workGroup=new NioEventLoopGroup();
        //EventLoopGroup bossGroup=new EpollEventLoopGroup(1);
        //EventLoopGroup workGroup=new EpollEventLoopGroup();
       // Thread.sleep(1000);
        ProviderChannelManager.initChannel(workGroup);

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
                    .childOption(ChannelOption.SO_RCVBUF, 64*1024)
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
        SimpleRegistryUtil.registerProvider(System.getProperty("etcd.url"));
        TCPProviderAgent tcpProviderAgent=new TCPProviderAgent();
        tcpProviderAgent.start(30000);
    }
}
