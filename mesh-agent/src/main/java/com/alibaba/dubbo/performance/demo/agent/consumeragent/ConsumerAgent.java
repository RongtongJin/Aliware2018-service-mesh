package com.alibaba.dubbo.performance.demo.agent.consumeragent;

import com.alibaba.dubbo.performance.demo.agent.protocal.MyHttpRequestDecoder;
import com.alibaba.dubbo.performance.demo.agent.protocal.MyHttpResponseEncoder;
import com.alibaba.dubbo.performance.demo.agent.protocal.TramissionHandler;
import com.alibaba.dubbo.performance.demo.agent.utils.SimpleRegistryUtil;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ConsumerAgent {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConsumerAgent.class);
    private static TCPChannelGroup channelGroup = null;
    //private IRegistry registry = new EtcdRegistry(System.getProperty("etcd.url"));

    public static TCPChannelGroup getTCPChannelGroup() {
        return channelGroup;
    }

    public static void main(String[] args) throws Exception {
        TramissionHandler.cacheEndpoints(System.getProperty("etcd.url"));

        boolean epollAvail = Epoll.isAvailable();
        EventLoopGroup bossGroup = null;
        EventLoopGroup workerGroup = null;
        Class<? extends ServerChannel> channelClass = null;
        if (epollAvail) {
            bossGroup = new EpollEventLoopGroup(1);
            workerGroup = new EpollEventLoopGroup();
            channelClass = EpollServerSocketChannel.class;
        } else {
            bossGroup = new NioEventLoopGroup(1);
            workerGroup = new NioEventLoopGroup(8);
            channelClass = NioServerSocketChannel.class;
        }


        TramissionHandler.iniClients(workerGroup);
        TramissionHandler.startClients();

        ConsumerAgent consumerAgent = new ConsumerAgent();
        consumerAgent.start0(20000,bossGroup,workerGroup,channelClass);
    }
    public void start(int port) throws  Exception{

        TramissionHandler.cacheEndpoints(System.getProperty("etcd.url"));

        boolean epollAvail = Epoll.isAvailable();
        EventLoopGroup bossGroup = null;
        EventLoopGroup workerGroup = null;
        Class<? extends ServerChannel> channelClass = null;

        if (epollAvail) {
            bossGroup = new EpollEventLoopGroup(1);
            workerGroup = new EpollEventLoopGroup();
            channelClass = EpollServerSocketChannel.class;
        } else {
            bossGroup = new NioEventLoopGroup(1);
            workerGroup = new NioEventLoopGroup(8);
            channelClass = NioServerSocketChannel.class;
        }


        TramissionHandler.iniClients(workerGroup);
        TramissionHandler.startClients();

        ConsumerAgent consumerAgent = new ConsumerAgent();
        consumerAgent.start0(port,bossGroup,workerGroup,channelClass);
    }
    public void start0(int port,EventLoopGroup bossGroup,EventLoopGroup workerGroup,Class<? extends ServerChannel> channelClass ) throws Exception {
        //UDPChannelManager.initChannel(workerGroup);
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup).channel(channelClass)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            // server端发送的是httpResponse，所以要使用HttpResponseEncoder进行编码
                            ch.pipeline().addLast("decoder", new MyHttpRequestDecoder());
                            // server端接收到的是httpRequest，所以要使用HttpRequestDecoder进行解码
                            ch.pipeline().addLast("encoder", new MyHttpResponseEncoder());
                            //fix me:设置的最大长度会不会影响性能
                            // ch.pipeline().addLast(new HttpObjectAggregator(2048));
                            ch.pipeline().addLast();
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 2048)
                    .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                    .childOption(ChannelOption.SO_REUSEADDR, Boolean.TRUE)
                    .childOption(ChannelOption.SO_RCVBUF, 64 * 1024)
                    .childOption(ChannelOption.ALLOW_HALF_CLOSURE, Boolean.FALSE);
            ChannelFuture f = b.bind(port).sync();
            if (f.isSuccess()) {
                LOGGER.info("ConsumerAgent start on " + port);
                f.channel().closeFuture().sync();
            }

        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }
}
