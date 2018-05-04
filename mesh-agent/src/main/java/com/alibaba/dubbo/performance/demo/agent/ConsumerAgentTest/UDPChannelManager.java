package com.alibaba.dubbo.performance.demo.agent.ConsumerAgentTest;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;

import java.net.InetSocketAddress;
import java.util.concurrent.CountDownLatch;


public class UDPChannelManager  implements Runnable{
    //是否要考虑利用父channel的eventLoopGroup

    private static CountDownLatch latch = new CountDownLatch(1);

    private static Channel channel=null;


    private void initChannel() throws Exception{
        System.out.println("UDPChannelManager()1");
        channel=new Bootstrap()
                .group(new NioEventLoopGroup())
                .channel(NioDatagramChannel.class)
                //.option(ChannelOption.SO_BACKLOG, 128)    //设置缓存队列
                //.option(ChannelOption.SO_RCVBUF, 1024 * 1024)// 设置UDP读缓冲区为1M
                //.option(ChannelOption.SO_SNDBUF, 1024 * 1024)// 设置UDP写缓冲区为1M ;
                .handler(new UDPChannelInitialzer())
                .bind(new InetSocketAddress(8087)).sync().channel();
        latch.countDown();
        System.out.println("UDPChannelManager()2");
        //channel.closeFuture().await();
        System.out.println("UDPChannelManager()3");
    }

    public static Channel getChannel(){
        //
        try {
            if (channel==null)
                latch.await();
        }catch (Exception e){
            e.printStackTrace();
        }
        return channel;
    }

    @Override
    public void run() {
        try {
            initChannel();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
