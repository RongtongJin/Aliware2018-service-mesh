package com.alibaba.dubbo.performance.demo.agent.provideragent;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

public class ProviderChannel {
    private Channel channel=null;

    public void initChannel(EventLoopGroup group) throws Exception{
        int port = Integer.valueOf(System.getProperty("dubbo.protocol.port"));
        //int port=20889;
        Class<? extends SocketChannel> channelClass= Epoll.isAvailable() ? EpollSocketChannel.class:NioSocketChannel.class;
        Bootstrap bootstrap = new Bootstrap()
                .group(group)
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
                });
        Boolean isConnect=false;
        while (!isConnect){
            try {
                channel=bootstrap.connect("127.0.0.1",20880).sync().channel();
                isConnect=true;
            }catch (Exception e){
                Thread.sleep(250);
            }
        }
    }

    public Channel getChannel() throws Exception{
        return channel;
    }
}
