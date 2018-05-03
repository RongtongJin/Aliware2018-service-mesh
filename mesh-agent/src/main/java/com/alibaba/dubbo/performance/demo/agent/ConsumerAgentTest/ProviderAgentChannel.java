package com.alibaba.dubbo.performance.demo.agent.ConsumerAgentTest;


import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.undertow.server.handlers.proxy.LoadBalancingProxyClient;


public class ProviderAgentChannel {
    private EventLoopGroup workGroup=null;
    private Channel channel=null;
    private Bootstrap bootstrap=null;
    private Object lock=new Object();
    private String host;
    private int port;

    ProviderAgentChannel(EventLoopGroup workGroup,String host,int port){
        this.workGroup=workGroup;
        this.host=host;
        this.port=port;
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
            synchronized (lock){
                if (null == channel){
                    channel = bootstrap.connect(host, port).sync().channel();
                }
            }
        }

        return channel;
    }

    public void initBootstrap() {

        bootstrap = new Bootstrap()
                .group(workGroup)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.TCP_NODELAY, true)
                .channel(NioSocketChannel.class)
                .handler(new RemoteInitializerTest(channel));
    }
}
