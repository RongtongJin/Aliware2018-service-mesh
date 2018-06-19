package com.alibaba.dubbo.performance.demo.agent.consumeragent.tcp;

import com.alibaba.dubbo.performance.demo.agent.consumeragent.common.MeshHttpRequestDecoder;
import com.alibaba.dubbo.performance.demo.agent.loadbalance.RandomLoadBalance;
import com.alibaba.dubbo.performance.demo.agent.registry.EtcdRegistry;
import com.alibaba.dubbo.performance.demo.agent.utils.ConstUtil;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.ServerChannel;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollChannelOption;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;


public class TcpConsumerAgent {

    private EtcdRegistry registry = new EtcdRegistry(System.getProperty("etcd.url"));

    public static void main(String[] args) throws Exception {
        new TcpConsumerAgent().start(20000);
    }

    public void start(int port) throws Exception {

        boolean epollAvail = Epoll.isAvailable();
        EventLoopGroup bossGroup = null;
        EventLoopGroup workerGroup = null;
        if (epollAvail) {
            bossGroup = new EpollEventLoopGroup(1);
            workerGroup = new EpollEventLoopGroup(3);
        } else {
            bossGroup = new NioEventLoopGroup(1);
            workerGroup = new NioEventLoopGroup(3);
        }
        Class<? extends ServerChannel> channelClass = epollAvail ? EpollServerSocketChannel.class : NioServerSocketChannel.class;


        registry.find("com.alibaba.dubbo.performance.demo.provider.IHelloService", RandomLoadBalance.endpoints);
        registry.register("lsx",9000);
        //初始化channel
        RandomLoadBalance.init(workerGroup);

        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(channelClass)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
//                            ch.pipeline().addLast(new LoggingHandler(LogLevel.INFO));
                            ch.pipeline().addLast(ConstUtil.DECODER, new MeshHttpRequestDecoder());
                            ch.pipeline().addLast(new MeshHttpMsgHandler());
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 512)
                    .option(ChannelOption.RCVBUF_ALLOCATOR, ConstUtil.LARGE_BUF_ALLOCATOR)
                    .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    .childOption(ChannelOption.RCVBUF_ALLOCATOR, ConstUtil.LARGE_BUF_ALLOCATOR)
                    .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                    .childOption(ChannelOption.SO_REUSEADDR, Boolean.TRUE)
                    .childOption(EpollChannelOption.TCP_QUICKACK,Boolean.TRUE)
                    .childOption(ChannelOption.ALLOW_HALF_CLOSURE, Boolean.FALSE);

            ChannelFuture f = b.bind(port).sync();
            System.out.println("TcpConsumerAgent start on " + port);
            f.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

}
