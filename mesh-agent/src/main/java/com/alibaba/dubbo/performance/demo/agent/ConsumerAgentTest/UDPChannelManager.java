package com.alibaba.dubbo.performance.demo.agent.ConsumerAgentTest;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;

import java.net.InetSocketAddress;


public class UDPChannelManager {
    //是否要考虑利用父channel的eventLoopGroup

    private static volatile Channel channel=null;
    private static Object lock = new Object();

    public UDPChannelManager(){

    }

    public static Channel getChannel() throws Exception {

        if (null == channel) {
            synchronized (lock){
                if (null == channel){
                    channel = new Bootstrap()
                            .group(new NioEventLoopGroup())
                            .channel(NioDatagramChannel.class)
                            //.option(ChannelOption.SO_BACKLOG, 128)    //设置缓存队列
                            //.option(ChannelOption.SO_RCVBUF, 1024 * 1024)// 设置UDP读缓冲区为1M
                            //.option(ChannelOption.SO_SNDBUF, 1024 * 1024)// 设置UDP写缓冲区为1M ;
                            .handler(new UDPChannelInitialzer())
                            .bind(new InetSocketAddress(8087)).sync().channel();
                }
            }
        }
        return channel;
    }

}