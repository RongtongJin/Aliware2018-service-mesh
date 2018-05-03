package com.alibaba.dubbo.performance.demo.agent.ComsumerAgent;

import com.alibaba.dubbo.performance.demo.agent.dubbo.model.Bytes;
import com.alibaba.fastjson.JSON;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.*;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

public class RemoteInboundHandler extends ChannelInboundHandlerAdapter {
    ChannelHandlerContext parentCtx;

    RemoteInboundHandler(ChannelHandlerContext ctx){
        this.parentCtx=ctx;
    }
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof HttpResponse)
        {
            HttpResponse response = (HttpResponse) msg;
            //System.out.println("CONTENT_TYPE:" + response.headers().get(HttpHeaders.Names.CONTENT_TYPE));
        }
        if(msg instanceof HttpContent)
        {
            HttpContent content = (HttpContent)msg;
            ByteBuf buf = content.content();
            //fix me?是否要从readindex和length中进行解析
            int length=buf.readableBytes();
            byte[] array=new byte[length];
            buf.getBytes(buf.readerIndex(),array);
            Integer res=JSON.parseObject(array, Integer.class);
            buf.release();
            byte[] ans=new byte[4];
            Bytes.int2bytes(res,ans,0);
            FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1,
                    OK, Unpooled.wrappedBuffer(ans));
            response.headers().set(CONTENT_TYPE, "text/plain");
            response.headers().set(CONTENT_LENGTH,
                    response.content().readableBytes());
            ctx.write(response);
            ctx.flush();
        }
    }
}
