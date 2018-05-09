package com.alibaba.dubbo.performance.demo.agent.consumeragent;

import com.alibaba.dubbo.performance.demo.agent.registry.Endpoint;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;

import io.netty.channel.socket.DatagramPacket;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.net.InetAddress;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;


public class ConsumerMsgHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private static Log log = LogFactory.getLog(ConsumerMsgHandler.class);

    private static AtomicLong genId=new AtomicLong();

    private List<Endpoint> endpoints=null;

    private Random random = new Random();

    public ConsumerMsgHandler(List<Endpoint> endpoints){
        this.endpoints=endpoints;
    }


    @Override
    public void channelRead0(ChannelHandlerContext ctx, FullHttpRequest msg) throws Exception{

        ByteBuf buf = msg.content();
        //System.out.println(buf.toString(io.netty.util.CharsetUtil.UTF_8));
        //buf.retain();
//        udpChannel=UDPChannelManager.getChannel();

 //       Long id=genId.getAndIncrement();
//
//        //fix me:存储如此多的id会不会成为性能瓶颈？？或者ConcurrentHashMap能不能进行优化
//        ChannelHolder.put(id,ctx.channel());
//        //fix me:为什么不能用CompositeByteBuf
//        CompositeByteBuf sendBuf=ctx.alloc().compositeBuffer();
//        ByteBuf idBuf=ctx.alloc().ioBuffer();
//        idBuf.writeLong(id);
//        sendBuf.addComponents(idBuf,need);
//        //fix me:用直接内存好还是heap内存好？HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE
       // ByteBuf byteBuf=ctx.alloc().ioBuffer(8+buf.readableBytes()-136);
 //       byteBuf.writeLong(id);
        //byteBuf.writeBytes(buf,136,buf.readableBytes()-136);

        buf.readerIndex(136);
        byte[] content=new byte[buf.readableBytes()];
        buf.readBytes(content);
        String str=new String(content);

        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1,
                OK, Unpooled.wrappedBuffer(Integer.toString(str.hashCode()).getBytes()));

        //需要加这个吗？
        response.headers().set(CONTENT_TYPE, "text/plain");
        response.headers().set(CONTENT_LENGTH,
                response.content().readableBytes());
        response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        ctx.writeAndFlush(response);

        //测试代码
     //   Endpoint endpoint=null;
        //负载均衡代码
        //按照性能简单负载均衡,这里有问题，并不清楚提供服务的机器性能
//        long x=id%6;
//        if(x==0)
//            endpoint=endpoints.get(0);
//        else if(1<=x&&x<=2)
//            endpoint=endpoints.get(1);
//        else
//            endpoint=endpoints.get(2);

        // 简单的负载均衡，随机取一个
//        Endpoint endpoint = endpoints.get(random.nextInt(endpoints.size()));
//
//        System.out.println(endpoint.getHost()+":"+endpoint.getPort());
//
//        DatagramPacket dp=new DatagramPacket(byteBuf,new java.net.InetSocketAddress(endpoint.getHost(),endpoint.getPort()));
//
//
//        UDPChannelManager.getChannel().write(dp).addListener(cf -> {
//            if (!cf.isSuccess()) {
//                log.error("error in udpChannel write.");
//                cf.cause().printStackTrace();
//            }
//        });
    }

//    @Override
//    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
//        UDPChannelManager.getChannel().flush();
//    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        //ctx.close();
    }

}
