package com.alibaba.dubbo.performance.demo.agent.provideragent;

import com.alibaba.dubbo.performance.demo.agent.registry.EtcdRegistry;
import com.alibaba.dubbo.performance.demo.agent.registry.IRegistry;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;

import java.net.InetSocketAddress;

public class ProviderAgent {

    //fix me:需要加volatie关键字吗？
    private static volatile Channel channel=null;

    //private IRegistry registry = new EtcdRegistry(System.getProperty("etcd.url"));


    public void start(int port) throws Exception{
        EventLoopGroup eventLoopGroup=new NioEventLoopGroup();
        try {
            Bootstrap bootstrap=new Bootstrap()
                    .group(eventLoopGroup)
                    .channel(NioDatagramChannel.class)
                    //.option(ChannelOption.SO_BACKLOG, 128)    //设置缓存队列
                    //.option(ChannelOption.SO_RCVBUF, 1024 * 1024)// 设置UDP读缓冲区为1M
                    //.option(ChannelOption.SO_SNDBUF, 1024 * 1024)// 设置UDP写缓冲区为1M
                    .handler(new ConsumerAgentMsgHandler());
            channel=bootstrap.bind(new InetSocketAddress(port)).sync().channel();
           // ProviderChannelManager.setUDPChannel(channel);
            channel.closeFuture().await();
        }finally {
            eventLoopGroup.shutdownGracefully();
        }
    }

    public static Channel getUDPChannel(){
        return channel;
    }

    public static void main(String[] args) throws Exception{
        new ProviderAgent().start(8844);
    }
}
