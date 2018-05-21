package com.alibaba.dubbo.performance.demo.agent.consumeragent.udp;

import com.alibaba.dubbo.performance.demo.agent.consumeragent.tcp.TcpChannel;
import com.alibaba.dubbo.performance.demo.agent.registry.Endpoint;
import com.alibaba.dubbo.performance.demo.agent.registry.EtcdRegistry;
import com.alibaba.dubbo.performance.demo.agent.registry.IRegistry;
import com.alibaba.dubbo.performance.demo.agent.registry.IpHelper;
import com.alibaba.dubbo.performance.demo.agent.utils.ConstUtil;
import com.alibaba.dubbo.performance.demo.agent.utils.EnumKey;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;

import java.net.InetSocketAddress;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public class UdpConsumerAgent {

    private IRegistry registry = new EtcdRegistry(System.getProperty("etcd.url"));
    private Map<EnumKey,Endpoint> endpoints = null;
    private Map<EnumKey,InetSocketAddress> udpChannelMap;

    public void start(int port) throws Exception {

        endpoints = registry.find("com.alibaba.dubbo.performance.demo.provider.IHelloService");

        boolean epollAvail= Epoll.isAvailable();
        EventLoopGroup bossGroup=null;
        EventLoopGroup workerGroup=null;
        if(epollAvail){
            bossGroup = new EpollEventLoopGroup(1);
            workerGroup = new EpollEventLoopGroup(8);
        }else{
            bossGroup = new NioEventLoopGroup(1);
            workerGroup = new NioEventLoopGroup(8);
        }
        Class<? extends ServerChannel> channelClass = epollAvail ? EpollServerSocketChannel.class : NioServerSocketChannel.class;

        if(!ConstUtil.IDEA_MODE){
            Thread.sleep(15000);
        }


        udpChannelMap=new EnumMap<>(EnumKey.class);
        for(Map.Entry<EnumKey,Endpoint> entry:endpoints.entrySet()){
            udpChannelMap.put(entry.getKey(),new InetSocketAddress(entry.getValue().getHost(),entry.getValue().getPort()));
        }


        UdpChannelManager.initChannel(workerGroup);

        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup).channel(channelClass)
                    //b.group(bossGroup, workerGroup).channel(EpollServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            // server端发送的是httpResponse，所以要使用HttpResponseEncoder进行编码
                            ch.pipeline().addLast(new HttpResponseEncoder());
                            // server端接收到的是httpRequest，所以要使用HttpRequestDecoder进行解码
                            ch.pipeline().addLast(new HttpRequestDecoder());
                            //fix me:设置的最大长度会不会影响性能
                            ch.pipeline().addLast(new HttpObjectAggregator(2048));
                            ch.pipeline().addLast(new UdpConsumerMsgHandler(udpChannelMap));

                        }
                    })
                    .option(ChannelOption.SO_BACKLOG,128)
                    .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childOption(ChannelOption.TCP_NODELAY,true)
                    .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                    .childOption(ChannelOption.SO_REUSEADDR, Boolean.TRUE)
                    .childOption(ChannelOption.ALLOW_HALF_CLOSURE, Boolean.FALSE);
            ChannelFuture f = b.bind(port).sync();
            System.out.println("UdpConsumerAgent start on "+port);
            f.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }


    public static void main(String[] args) throws Exception{
        new UdpConsumerAgent().start(20000);
    }

}