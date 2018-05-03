package com.alibaba.dubbo.performance.demo.agent.ConsumerAgentTest;

import com.alibaba.dubbo.performance.demo.agent.dubbo.model.Bytes;
import com.alibaba.fastjson.JSON;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;

import static io.netty.handler.codec.http.HttpHeaders.Names.CONNECTION;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

public class RemoteInboundHandlerTest extends SimpleChannelInboundHandler<FullHttpResponse> {
    Channel parentChannel;

    RemoteInboundHandlerTest(Channel parentChannel){
        this.parentChannel=parentChannel;
    }
    @Override
    public void channelRead0(ChannelHandlerContext ctx, FullHttpResponse msg) throws Exception {

        System.out.println("response");
        ByteBuf buf = msg.content();
        System.out.println(buf.toString(io.netty.util.CharsetUtil.UTF_8));

        String res = "I am OK";
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1,
                OK, Unpooled.wrappedBuffer(res.getBytes("UTF-8")));
        response.headers().set(CONTENT_TYPE, "text/plain");
        response.headers().set(CONTENT_LENGTH,
                response.content().readableBytes());
        parentChannel.writeAndFlush(response).addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                if(!channelFuture.isSuccess()){
                    System.err.println("write failed!");
                    channelFuture.cause().printStackTrace();
                }
            }
        });
    }

}