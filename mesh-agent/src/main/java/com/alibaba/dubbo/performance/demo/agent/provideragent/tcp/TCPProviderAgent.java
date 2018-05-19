package com.alibaba.dubbo.performance.demo.agent.provideragent.tcp;

import com.alibaba.dubbo.performance.demo.agent.provideragent.ProviderChannelManager;
import com.alibaba.dubbo.performance.demo.agent.provideragent.TCPConsumerAgentMsgHandler;
import com.alibaba.dubbo.performance.demo.agent.utils.SimpleRegistryUtil;
import com.alibaba.dubbo.performance.demo.agent.utils.TcpConnectUtil;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TCPProviderAgent {
    private static Channel channel=null;
    private static  final Logger LOGGER= LoggerFactory.getLogger(TCPProviderAgent.class);
    public void start0(int port) throws Exception{
        boolean epollAvail= Epoll.isAvailable();
        EventLoopGroup bossGroup=null;
        EventLoopGroup workerGroup=null;
        Class<? extends ServerChannel> channelClass=null;
        if(epollAvail){
            bossGroup = new EpollEventLoopGroup(1);
            workerGroup = new EpollEventLoopGroup();
            channelClass= EpollServerSocketChannel.class;
        }else{
            bossGroup = new NioEventLoopGroup(1);
            workerGroup = new NioEventLoopGroup();
            channelClass=NioServerSocketChannel.class;
        }
        while(!TcpConnectUtil.isHostConnectable("127.0.0.1",20880)){
            Thread.sleep(1000);
        }
//        group=new ProviderChannelGroup(13,workGroup);
        TCPProviderChannelManager.initChannel(workerGroup);

        try {
            channel = new ServerBootstrap()
                    .group(bossGroup,workerGroup)
                    .channel(channelClass)
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
            workerGroup.shutdownGracefully();
        }
    }

    public void start(int port) throws Exception{
        SimpleRegistryUtil.registerProvider(System.getProperty("etcd.url"));
        TCPProviderAgent tcpProviderAgent=new TCPProviderAgent();
        tcpProviderAgent.start0(port);
    }
}
