package com.alibaba.dubbo.performance.demo.agent.consumeragent.tcp;

import com.alibaba.dubbo.performance.demo.agent.consumeragent.model.ChannelHolder;
import com.alibaba.dubbo.performance.demo.agent.registry.Endpoint;
import com.alibaba.dubbo.performance.demo.agent.utils.EnumKey;
import io.netty.buffer.*;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;


public class TcpConsumerMsgHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private static Log log = LogFactory.getLog(TcpConsumerMsgHandler.class);

    private static AtomicLong genId=new AtomicLong();

    private static Random random = new Random();

    private Map<EnumKey,TcpChannel> tcpChannelMap;


    public TcpConsumerMsgHandler(Map<EnumKey,TcpChannel> tcpChannelMap){
        this.tcpChannelMap=tcpChannelMap;
    }


    @Override
    public void channelRead0(ChannelHandlerContext ctx, FullHttpRequest msg) throws Exception{

        ByteBuf buf = msg.content();
//        System.out.println(buf.toString(io.netty.util.CharsetUtil.UTF_8));
        long id=genId.getAndIncrement();

        ChannelHolder.put(id,ctx.channel());

//        PooledByteBufAllocator.DEFAULT.
        CompositeByteBuf sendBuf=ctx.alloc().compositeDirectBuffer();
        ByteBuf idBuf=ctx.alloc().ioBuffer();
        idBuf.writeLong(id);
        sendBuf.addComponents(true,idBuf,buf.slice(136,buf.readableBytes()-136).retain());
        //sendBuf.writeBytes(System.lineSeparator().getBytes());

        TcpChannel ch=null;


        //tcp按照性能简单负载均衡,fix me:利用id 可以不生成随机数
//        int x=random.nextInt(6);
//        if(x==0){
//            ch=tcpChannelMap.get("small");
//        }else if(x<=2){
//            ch=tcpChannelMap.get("medium");
//        }else{
//            ch=tcpChannelMap.get("large");
//        }
        ch=tcpChannelMap.get(EnumKey.getNext((int)id));

        //idea下测试使用tcp
      //  ch=tcpChannelMap.get("ideaTest");


        /*tcp发给provider agent*/
        System.out.println("send start..");
        ch.getChannel().writeAndFlush(sendBuf);
        System.out.println("send finish..");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        //ctx.close();
    }

}
