package com.alibaba.dubbo.performance.demo.agent.consumeragent;

import com.alibaba.dubbo.performance.demo.agent.utils.Bytes;
import com.alibaba.fastjson.JSON;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

public class ProviderAgentMsgHandler extends SimpleChannelInboundHandler<DatagramPacket> {
    private static Log log = LogFactory.getLog(ProviderAgentMsgHandler.class);
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, DatagramPacket datagramPacket) throws Exception {
        ByteBuf buf=datagramPacket.content();
        Long id=buf.readLong();
        buf.retain();
        Channel sendChannel=ChannelHolder.get(id);
        //fix me：获取后是否需要删除，删除可能会影响性能，不删除可能会影响GC
        ChannelHolder.remove(id);
        //是否要加这个连接判断
//        byte[] bytes=new byte[buf.readableBytes()];
//        buf.readBytes(bytes);
//        Integer res= JSON.parseObject(bytes, Integer.class);
       // System.out.println(id);
        ByteBuf hashCodeBuf = buf.slice(8,buf.readableBytes());
       // System.out.println(hashCodeBuf.toString(io.netty.util.CharsetUtil.UTF_8));
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1,
                OK, hashCodeBuf);

        //需要加这个吗？
        response.headers().set(CONTENT_TYPE, "text/plain");
        response.headers().set(CONTENT_LENGTH,
                response.content().readableBytes());
        //response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        sendChannel.writeAndFlush(response).addListener(cf->{
            if(!cf.isSuccess()){
                log.error("send msg to Consumer failed.");
                cf.cause().printStackTrace();
            }
        });

    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        //super.exceptionCaught(ctx, cause);
        cause.printStackTrace();
        //ctx.close();
    }
}
