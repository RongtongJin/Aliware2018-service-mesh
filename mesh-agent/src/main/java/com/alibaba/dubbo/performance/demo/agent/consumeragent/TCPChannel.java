package com.alibaba.dubbo.performance.demo.agent.consumeragent;

import com.alibaba.dubbo.performance.demo.agent.registry.Endpoint;
import com.alibaba.dubbo.performance.demo.agent.utils.TcpConnectTest;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;

import java.util.concurrent.TimeUnit;


public class TCPChannel {
    private Channel channel=null;

    public TCPChannel(EventLoopGroup group, Endpoint endpoint) throws Exception{

//        while(!TcpConnectTest.isHostConnectable(endpoint.getHost(),endpoint.getPort())){
//            Thread.sleep(1000);
//        }

        Class<? extends SocketChannel> channelClass=Epoll.isAvailable() ? EpollSocketChannel.class:NioSocketChannel.class;
        Bootstrap bootstrap = new Bootstrap()
                .group(group)
                .channel(channelClass)
                //.channel(EpollSocketChannel.class)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.TCP_NODELAY, true)
                //.option(ChannelOption.SO_SNDBUF,1024*1024)
                //.option(ChannelOption.SO_RCVBUF,100*1024)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        ChannelPipeline pipeline = socketChannel.pipeline();
                        pipeline.addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE,0,2,0,2));
                        pipeline.addLast(new LengthFieldPrepender(2,false));
                        pipeline.addLast(new TCPProviderAgentMsgHandler());
                    }
                });
        Boolean isConnect=false;
        while (!isConnect){
            try {
                channel=bootstrap.connect(endpoint.getHost(),endpoint.getPort()).sync().channel();
                isConnect=true;
            }catch (Exception e){
                Thread.sleep(250);
            }
        }
    }

    public Channel getChannel() throws Exception {
        return channel;
    }
}
