package com.alibaba.dubbo.performance.demo.agent.consumeragent.udp;

import com.alibaba.dubbo.performance.demo.agent.consumeragent.model.ChannelHolder;
import com.alibaba.dubbo.performance.demo.agent.provideragent.udp.UdpProviderAgent;
import com.alibaba.dubbo.performance.demo.agent.registry.Endpoint;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.CompositeByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.http.FullHttpRequest;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

public class UdpConsumerMsgHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    private static AtomicLong genId=new AtomicLong();

    private Map<String,InetSocketAddress> udpChannelMap;

    private static Random random = new Random();


    public UdpConsumerMsgHandler(Map<String,InetSocketAddress> udpChannelMap){
        this.udpChannelMap=udpChannelMap;
    }


    @Override
    public void channelRead0(ChannelHandlerContext ctx, FullHttpRequest msg) throws Exception{
        ByteBuf buf = msg.content();
//        System.out.println(buf.toString(io.netty.util.CharsetUtil.UTF_8));
        Long id=genId.getAndIncrement();
        ChannelHolder.put(id,ctx.channel());
//        PooledByteBufAllocator.DEFAULT.
        CompositeByteBuf sendBuf=ctx.alloc().compositeDirectBuffer();
        ByteBuf idBuf=ctx.alloc().ioBuffer();
        idBuf.writeLong(id);
        sendBuf.addComponents(true,idBuf,buf.slice(136,buf.readableBytes()-136).retain());
        //sendBuf.writeBytes(System.lineSeparator().getBytes());
        InetSocketAddress addr=null;
        /*负载均衡代码*/
        //udp按照性能简单负载均衡,fix me:利用id 可以不生成随机数

        int x=random.nextInt(6);
        if(x==0){
            addr=udpChannelMap.get("small");
        }else if(x<=2){
            addr=udpChannelMap.get("medium");
        }else{
            addr=udpChannelMap.get("large");
        }

        //idea下测试使用udp
//        addr=udpChannelMap.get("ideaTest");

        DatagramPacket dp=new DatagramPacket(sendBuf,addr);

        UdpChannelManager.getChannel().writeAndFlush(dp).addListener(cf -> {
            if (!cf.isSuccess()) {
                cf.cause().printStackTrace();
            }
        });
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        //ctx.close();
    }
}
