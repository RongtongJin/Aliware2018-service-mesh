package com.alibaba.dubbo.performance.demo.agent.provideragent.tcp;

import com.alibaba.dubbo.performance.demo.agent.registry.EtcdRegistry;
import com.alibaba.dubbo.performance.demo.agent.registry.IRegistry;
import com.alibaba.dubbo.performance.demo.agent.utils.ConstUtil;
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

public class TcpProviderAgent {

    private static Channel channel=null;

    private IRegistry registry = new EtcdRegistry(System.getProperty("etcd.url"));

    private static Channel consumerAgentChannel=null;

    public static void setConsumerAgentChannel(Channel ch){
        consumerAgentChannel=ch;
    }

    public static Channel getConsumerAgentChannel(){return consumerAgentChannel;}

    public void start(int port) throws Exception{

        boolean epollAvail=Epoll.isAvailable();
        EventLoopGroup bossGroup=null;
        EventLoopGroup workerGroup=null;
        if(epollAvail){
            bossGroup = new EpollEventLoopGroup(1);
            workerGroup = new EpollEventLoopGroup(2);
        }else{
            bossGroup = new NioEventLoopGroup(1);
            workerGroup = new NioEventLoopGroup(2);
        }
        Class<? extends ServerChannel> channelClass = epollAvail ? EpollServerSocketChannel.class : NioServerSocketChannel.class;

        if(!ConstUtil.IDEA_MODE){
            Thread.sleep(16000);
        }

        TcpProviderChannelManager.initChannel(workerGroup);

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
                    //.childOption(EpollChannelOption.TCP_QUICKACK,Boolean.TRUE)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            ChannelPipeline pipeline = socketChannel.pipeline();
                            pipeline.addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE,0,2,0,2));
                            pipeline.addLast(new LengthFieldPrepender(1,false));
                            pipeline.addLast(new TcpConsumerAgentMsgHandler());
                        }
                    }).bind(port).sync().channel();
            System.out.println("TcpProviderAgent start on "+port);
            channel.closeFuture().await();
        }finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws Exception{
        new TcpProviderAgent().start(30000);
    }
}
