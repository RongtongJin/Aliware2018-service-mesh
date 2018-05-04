package com.alibaba.dubbo.performance.demo.agent.provideragent;



import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;


/**
 * Created by 79422 on 2018/5/4.
 */
public class ProviderChannelManager{

    private static Channel udpChannel=null;
    private static volatile Channel channel=null;
    private static Object lock = new Object();


    public static void setUDPChannel(Channel ch){
        //fix me:是否可以优化，因为每次需要多判断一次是否等于null
        if (udpChannel==null){
            udpChannel=ch;
        }
    }

    public static Channel getChannel() throws Exception{
        if (null == channel) {
            synchronized (lock){
                if (null == channel){
                    //int port = Integer.valueOf(System.getProperty("dubbo.protocol.port"));
                    int port=22222;
                    channel = new Bootstrap()
                            .group(new NioEventLoopGroup())
                            .option(ChannelOption.SO_KEEPALIVE, true)
                            .option(ChannelOption.TCP_NODELAY, true)
                            .option(ChannelOption.ALLOCATOR, UnpooledByteBufAllocator.DEFAULT)
                            .channel(NioSocketChannel.class)
                            .handler(new RpcHandlerInitializer())
                            .connect("127.0.0.1", port).sync().channel();
                }
            }
        }
        return channel;
    }

}
