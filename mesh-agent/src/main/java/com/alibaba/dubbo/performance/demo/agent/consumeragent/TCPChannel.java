package com.alibaba.dubbo.performance.demo.agent.consumeragent;

import com.alibaba.dubbo.performance.demo.agent.registry.Endpoint;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;


public class TCPChannel {
    private Channel channel=null;

    public TCPChannel(){}

    public void initChannel(EventLoopGroup group, Endpoint endpoint) throws Exception{
        channel = new Bootstrap()
                .group(group)
                .channel(NioSocketChannel.class)
                //.channel(EpollSocketChannel.class)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.TCP_NODELAY, true)
                //.option(ChannelOption.SO_SNDBUF,1024*1024)
                //.option(ChannelOption.SO_RCVBUF,100*1024)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        ChannelPipeline pipeline = socketChannel.pipeline();
                        pipeline.addLast(new LineBasedFrameDecoder(30));
                        pipeline.addLast(new TCPProviderAgentMsgHandler());
                    }
                })
                .connect(endpoint.getHost(), endpoint.getPort()).sync().channel();
    }

    public Channel getChannel() throws Exception {
        return channel;
    }
}
