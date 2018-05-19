package com.alibaba.dubbo.performance.demo.agent.provideragent;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

public class TCPProviderChannelManager {
    private static Channel channel=null;
    private static EventLoopGroup workerGroup=null;
    private static Object lock = new Object();
    private static Bootstrap bootstrap;

    public static void setWorkerGroup(EventLoopGroup group){
        workerGroup=group;
    }


    public static void initBootstrap(){

        boolean epollAvail=Epoll.isAvailable();
        Class<? extends SocketChannel> channelClass = epollAvail ? EpollSocketChannel.class:NioSocketChannel.class;

        bootstrap = new Bootstrap()
                .group(workerGroup)
                .channel(channelClass)
                //.group(new EpollEventLoopGroup())
                //.channel(EpollSocketChannel.class)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.TCP_NODELAY, true)
                //.option(ChannelOption.CONNECT_TIMEOUT_MILLIS,17000)
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
                });
    }

    public static Channel getChannel() throws Exception{
        if (null != channel) {
            return channel;
        }

        if (null == bootstrap) {
            synchronized (lock) {
                if (null == bootstrap) {
                    initBootstrap();
                }
            }
        }

        if (null == channel) {
            synchronized (lock){
                if (null == channel){
                    channel = bootstrap.connect("127.0.0.1", 20880).sync().channel();
                }
            }
        }

        return channel;
    }
}
