package com.alibaba.dubbo.performance.demo.agent.consumeragent;

import com.alibaba.dubbo.performance.demo.agent.registry.Endpoint;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;

import io.netty.channel.socket.DatagramPacket;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.net.InetAddress;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;


public class ConsumerMsgHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private static Log log = LogFactory.getLog(ConsumerMsgHandler.class);

    private Channel udpChannel=null;

    private static AtomicLong genId=new AtomicLong();

    private List<Endpoint> endpoints=null;

    public ConsumerMsgHandler(List<Endpoint> endpoints){
        this.endpoints=endpoints;
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, FullHttpRequest msg) throws Exception{
        ByteBuf buf = msg.content();
        //System.out.println(buf.toString(io.netty.util.CharsetUtil.UTF_8));
        //buf.retain();
        udpChannel=UDPChannelManager.getChannel();

        Long id=genId.getAndIncrement();

        //fix me:存储如此多的id会不会成为性能瓶颈？？或者ConcurrentHashMap能不能进行优化
        ChannelHolder.put(id,ctx.channel());
        //fix me:为什么不能用CompositeByteBuf
        //CompositeByteBuf sendBuf=Unpooled.compositeBuffer();
        //fix me:用直接内存好还是heap内存好？
        ByteBuf byteBuf=Unpooled.directBuffer(8+buf.readableBytes());
        byteBuf.writeLong(id);
        byteBuf.writeBytes(buf);
        //sendBuf.addComponents(byteBuf,buf);

        //测试代码
        Endpoint endpoint=endpoints.get(0);
        //负载均衡代码
        //按照性能简单负载均衡
//        int x=id%6;
//        if(x==0)
//            endpoint=endpoints.get(0);
//        else if(1<=x&&x<=2)
//            endpoint=endpoints.get(1);
//        else
//            endpoint=endpoints.get(2);

        DatagramPacket dp=new DatagramPacket(byteBuf,new java.net.InetSocketAddress(endpoint.getHost(),endpoint.getPort()));
        if (udpChannel.isActive()) {
            udpChannel.writeAndFlush(dp).addListener(cf -> {
                if (!cf.isSuccess()) {
                    log.error("error in udpChannel write.");
                    cf.cause().printStackTrace();
                }
            });
        }
    }
}
