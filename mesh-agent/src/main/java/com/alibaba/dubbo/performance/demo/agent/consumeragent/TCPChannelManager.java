package com.alibaba.dubbo.performance.demo.agent.consumeragent;

import com.alibaba.dubbo.performance.demo.agent.provideragent.RpcHandlerInitializer;
import com.alibaba.dubbo.performance.demo.agent.registry.Endpoint;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;



public class TCPChannelManager {
    private static Channel channel=null;

    public TCPChannelManager(){}

    public static void initChannel(EventLoopGroup group, Endpoint endpoint) throws Exception{
        channel = new Bootstrap()
                .group(group)
                .channel(NioSocketChannel.class)
                //.channel(EpollSocketChannel.class)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.TCP_NODELAY, true)
                //.option(ChannelOption.ALLOCATOR, UnpooledByteBufAllocator.DEFAULT)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        ChannelPipeline pipeline = socketChannel.pipeline();
                        ByteBuf delimiter = Unpooled.wrappedBuffer("$".getBytes());
                        pipeline.addLast(new DelimiterBasedFrameDecoder(1024,delimiter));
                        pipeline.addLast(new TCPProviderAgentMsgHandler());
                    }
                })
                .connect(endpoint.getHost(), endpoint.getPort()).sync().channel();
    }

    public static Channel getChannel() throws Exception {
        return channel;
    }
}
