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
    private Bootstrap bootstrap;
    private Object lock = new Object();
    private EventLoopGroup workerGroup;
    private Endpoint endpoint;

    public TCPChannel(EventLoopGroup group, Endpoint endpoint){
        this.workerGroup=group;
        this.endpoint=endpoint;
    }

    public Channel getChannel() throws Exception {
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
            synchronized (lock) {
                if(null==channel){
                    channel=bootstrap.connect(endpoint.getHost(),endpoint.getPort()).sync().channel();
                }
            }
        }

        return channel;
    }

    public void initBootstrap(){
        Class<? extends SocketChannel> channelClass=Epoll.isAvailable() ? EpollSocketChannel.class:NioSocketChannel.class;
        bootstrap = new Bootstrap()
                .group(workerGroup)
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
    }
}
