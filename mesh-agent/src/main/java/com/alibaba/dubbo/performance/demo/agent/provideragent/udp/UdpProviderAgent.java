package com.alibaba.dubbo.performance.demo.agent.provideragent.udp;

import com.alibaba.dubbo.performance.demo.agent.registry.EtcdRegistry;
import com.alibaba.dubbo.performance.demo.agent.registry.IRegistry;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollDatagramChannel;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.nio.NioDatagramChannel;

import java.net.InetSocketAddress;

public class UdpProviderAgent {

    private static Channel channel=null;

    private static volatile InetSocketAddress msgReturner=null;

    private IRegistry registry = new EtcdRegistry(System.getProperty("etcd.url"));

    public void start(int port) throws Exception{

        boolean epollAvail= Epoll.isAvailable();

        EventLoopGroup eventLoopGroup=epollAvail ? new EpollEventLoopGroup(2): new NioEventLoopGroup(2);

        Class<? extends DatagramChannel> channelClass= epollAvail ? EpollDatagramChannel.class:NioDatagramChannel.class;

        Thread.sleep(1000);

        UdpProviderChannelManager.initChannel(eventLoopGroup);

        try {
            Bootstrap bootstrap=new Bootstrap()
                    .group(eventLoopGroup)
                    .channel(channelClass)
                    //.channel(EpollDatagramChannel.class)
                    //.option(ChannelOption.SO_BACKLOG, 1024)    //设置缓存队列
                    //.option(EpollChannelOption.SO_REUSEPORT,true)
                    .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                    //.option(ChannelOption.SO_RCVBUF)// 设置UDP读缓冲区为1M
                    //.option(ChannelOption.SO_SNDBUF) // 设置UDP写缓冲区为1M
                    .handler(new UdpConsumerAgentMsgHandler());
            channel=bootstrap.bind(new InetSocketAddress(port)).sync().channel();
            System.out.println("ProviderAgent start on "+port);
            channel.closeFuture().await();
        }catch (Exception e){

            e.printStackTrace();
        } finally {
            eventLoopGroup.shutdownGracefully();
        }
    }

    public static Channel getUdpChannel(){
        return channel;
    }

    public static void setMsgReturner(InetSocketAddress addr){
        if (msgReturner!=null) return;
        msgReturner=addr;
    }

    public static InetSocketAddress getMsgReturner(){return msgReturner;}

    public static void main(String[] args) throws Exception{
        new UdpProviderAgent().start(30000);
    }
}
