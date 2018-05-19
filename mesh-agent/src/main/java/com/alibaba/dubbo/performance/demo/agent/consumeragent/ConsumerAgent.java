package com.alibaba.dubbo.performance.demo.agent.consumeragent;

import com.alibaba.dubbo.performance.demo.agent.provideragent.ProviderAgent;
import com.alibaba.dubbo.performance.demo.agent.registry.Endpoint;
import com.alibaba.dubbo.performance.demo.agent.registry.EtcdRegistry;
import com.alibaba.dubbo.performance.demo.agent.registry.IRegistry;
import com.alibaba.dubbo.performance.demo.agent.registry.IpHelper;
import com.alibaba.dubbo.performance.demo.agent.utils.TcpConnectTest;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.epoll.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.omg.PortableInterceptor.USER_EXCEPTION;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConsumerAgent {

    private static Log log = LogFactory.getLog(ConsumerAgent.class);
    private IRegistry registry = new EtcdRegistry(System.getProperty("etcd.url"));
    private Map<String,Endpoint> endpoints = null;
    private static Map<String,TCPChannel> tcpChannelMap=null;
//    private static TCPChannelGroup channelGroup=null;
//
//
//    public static TCPChannelGroup getTCPChannelGroup(){return channelGroup;}

    public void start(int port) throws Exception {

        endpoints = registry.find("com.alibaba.dubbo.performance.demo.provider.IHelloService");

        boolean epollAvail=Epoll.isAvailable();
        EventLoopGroup bossGroup=null;
        EventLoopGroup workerGroup=null;
        if(epollAvail){
            bossGroup = new EpollEventLoopGroup(1);
            workerGroup = new EpollEventLoopGroup();
        }else{
            bossGroup = new NioEventLoopGroup(1);
            workerGroup = new NioEventLoopGroup();
        }
        Class<? extends ServerChannel> channelClass = epollAvail ? EpollServerSocketChannel.class : NioServerSocketChannel.class;

        Thread.sleep(15000);

        tcpChannelMap=new HashMap<>();
        for(Map.Entry<String,Endpoint> entry:endpoints.entrySet()){
            tcpChannelMap.put(entry.getKey(),new TCPChannel(workerGroup,entry.getValue()));
        }




        //IDEA TEST USE
        //tcpChannelMap.put("ideaTest",new TCPChannel(workerGroup,new Endpoint(IpHelper.getHostIp(),30000)));

        //UDPChannelManager.initChannel(workerGroup);

        //channelGroup=new TCPChannelGroup(12,workerGroup,new Endpoint(IpHelper.getHostIp(),30000));

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
                            ch.pipeline().addLast(new ConsumerMsgHandler(endpoints));
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG,128)
                    .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childOption(ChannelOption.TCP_NODELAY,true)
                    .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                    .childOption(ChannelOption.SO_REUSEADDR, Boolean.TRUE)
                    .childOption(ChannelOption.AUTO_CLOSE, Boolean.TRUE)
                    .childOption(ChannelOption.ALLOW_HALF_CLOSURE, Boolean.FALSE);
            ChannelFuture f = b.bind(port).sync();
            System.out.println("ConsumerAgent start on "+port);
            f.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

    public static Map<String,TCPChannel> getTcpChannelMap(){
        return tcpChannelMap;
    }

    public static void main(String[] args) throws Exception{
        new ConsumerAgent().start(20000);
    }

}
