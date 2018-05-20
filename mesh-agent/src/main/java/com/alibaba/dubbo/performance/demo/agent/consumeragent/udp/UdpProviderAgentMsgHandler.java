package com.alibaba.dubbo.performance.demo.agent.consumeragent.udp;

import com.alibaba.dubbo.performance.demo.agent.consumeragent.model.ChannelHolder;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

public class UdpProviderAgentMsgHandler extends SimpleChannelInboundHandler<DatagramPacket> {

    private static Log log = LogFactory.getLog(UdpProviderAgentMsgHandler.class);
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, DatagramPacket datagramPacket) throws Exception {
        ByteBuf buf=datagramPacket.content();
        Long id=buf.readLong();
        Channel sendChannel= ChannelHolder.get(id);
        //测试后发现每次remove id后性能更高
        ChannelHolder.remove(id);
        ByteBuf hashCodeBuf = buf.slice(8,buf.readableBytes()).retain();
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
        cause.printStackTrace();
        //ctx.close();
    }
}
