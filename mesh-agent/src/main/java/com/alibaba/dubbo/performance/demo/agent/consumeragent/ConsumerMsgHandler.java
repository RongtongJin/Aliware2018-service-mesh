package com.alibaba.dubbo.performance.demo.agent.consumeragent;

import com.alibaba.dubbo.performance.demo.agent.registry.Endpoint;
import com.alibaba.dubbo.performance.demo.agent.registry.IpHelper;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;

import io.netty.channel.socket.DatagramPacket;
import io.netty.util.CharsetUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.net.InetAddress;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;


public class ConsumerMsgHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private static Log log = LogFactory.getLog(ConsumerMsgHandler.class);

    private static AtomicLong genId=new AtomicLong();

    private Map<String,Endpoint> endpoints=null;

    private static Random random = new Random();

    private static Map<String,TCPChannel> tcpChannelMap=ConsumerAgent.getTcpChannelMap();


    public ConsumerMsgHandler(Map<String,Endpoint> endpoints){
        this.endpoints=endpoints;
    }


    @Override
    public void channelRead0(ChannelHandlerContext ctx, FullHttpRequest msg) throws Exception{

        ByteBuf buf = msg.content();
//        System.out.println(buf.toString(io.netty.util.CharsetUtil.UTF_8));
        buf.retain();

        Long id=genId.getAndIncrement();

        ChannelHolder.put(id,ctx.channel());

        CompositeByteBuf sendBuf=ctx.alloc().compositeDirectBuffer();
        ByteBuf idBuf=ctx.alloc().ioBuffer();
        idBuf.writeLong(id);
        ByteBuf paraBuf=buf.slice(136,buf.readableBytes()-136);
        sendBuf.addComponents(true,idBuf,paraBuf);
        //sendBuf.writeBytes(System.lineSeparator().getBytes());


        /*发给provider consumer做测试*/
//        ByteBuf sendBuf=buf.slice(136,buf.readableBytes()-136);
//
//        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1,
//                OK, Unpooled.wrappedBuffer(Integer.toString(sendBuf.toString(CharsetUtil.UTF_8).hashCode()).getBytes()));
//
//        //需要加这个吗？
//        response.headers().set(CONTENT_TYPE, "text/plain");
//        response.headers().set(CONTENT_LENGTH,
//                response.content().readableBytes());
//        ctx.writeAndFlush(response);


        /*负载均衡代码*/
        //udp按照性能简单负载均衡,fix me:利用id 可以不生成随机数
//        int x=random.nextInt(6);
//        Endpoint endpoint=null;
//        if(x==0){
//            endpoint=endpoints.get("small");
//        }else if(x<=2){
//            endpoint=endpoints.get("medium");
//        }else{
//            endpoint=endpoints.get("large");
//        }

//        tcp按照性能简单负载均衡,fix me:利用id 可以不生成随机数
        int x=random.nextInt(6);
        TCPChannel ch=null;
        if(x==0){
            ch=tcpChannelMap.get("small");
        }else if(x<=2){
            ch=tcpChannelMap.get("medium");
        }else{
            ch=tcpChannelMap.get("large");
        }



        //idea下测试使用udp
//        Endpoint endpoint=new Endpoint(IpHelper.getHostIp(),30000);

        //idea下测试使用tcp
 //       TCPChannel ch=tcpChannelMap.get("ideaTest");

        /*udp发给provider agent*/
        //简单的负载均衡，随机取一个
//        Endpoint endpoint = endpoints.get(random.nextInt(endpoints.size()));

//        DatagramPacket dp=new DatagramPacket(sendBuf,new java.net.InetSocketAddress(endpoint.getHost(),endpoint.getPort()));
//
//        UDPChannelManager.getChannel().writeAndFlush(dp).addListener(cf -> {
//            if (!cf.isSuccess()) {
//                log.error("error in udpChannel write.");
//                cf.cause().printStackTrace();
//            }
//        });

        /*tcp发给provider agent*/
//        Endpoint endpoint = endpoints.get(random.nextInt(endpoints.size()));
         // ConsumerAgent.getTCPChannelGroup().nextChannel().writeAndFlush(sendBuf);
        ch.getChannel().writeAndFlush(sendBuf);
//        System.out.println("send finish..");
    }

//    @Override
//    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
//        UDPChannelManager.getChannel().flush();
//    }
//
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        //ctx.close();
    }

}
