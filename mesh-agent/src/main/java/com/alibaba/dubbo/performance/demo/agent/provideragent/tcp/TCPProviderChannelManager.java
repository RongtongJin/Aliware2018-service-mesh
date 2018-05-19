package com.alibaba.dubbo.performance.demo.agent.provideragent.tcp;

import com.alibaba.dubbo.performance.demo.agent.provideragent.DubboRpcEncoder2;
import com.alibaba.dubbo.performance.demo.agent.provideragent.RpcMsgHandler3;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

public class TCPProviderChannelManager {
    private static volatile Channel channel=null;
    private static EventLoopGroup workerGroup=null;
    private static Object lock = new Object();


    public static void setWorkerGroup(EventLoopGroup group){
        workerGroup=group;
    }


    public static Channel getChannel() throws Exception{
        if (null != channel) {
            return channel;
        }

        if (null == channel) {
            synchronized (lock){
                if (null == channel){
                    Class<? extends SocketChannel> channelClass= Epoll.isAvailable() ? EpollSocketChannel.class:NioSocketChannel.class;
                    channel = new Bootstrap()
                            .group(workerGroup)
                            .channel(channelClass)
                            //.group(new EpollEventLoopGroup())
                            //.channel(EpollSocketChannel.class)
                            .option(ChannelOption.SO_KEEPALIVE, true)
                            .option(ChannelOption.TCP_NODELAY, true)
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
                            })
                            .connect("127.0.0.1", 20880).sync().channel();
                }
            }
        }

        return channel;
    }
}