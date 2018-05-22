package com.alibaba.dubbo.performance.demo.agent.consumeragent.tcp;

import com.alibaba.dubbo.performance.demo.agent.consumeragent.model.ChannelHolder;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;

import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

public class TcpProviderAgentMsgHandler extends SimpleChannelInboundHandler<ByteBuf> {

    //private static ExecutorService threadsPool= Executors.newSingleThreadExecutor();

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf) throws Exception {
        int id=byteBuf.readInt();
        Channel sendChannel= ChannelHolder.get(id);
        //测试后发现每次remove id后性能更高
        ChannelHolder.remove(id);
        //threadsPool.submit(new Task(id));
        //是否要加这个连接判断
//        byte[] bytes=new byte[buf.readableBytes()];
//        buf.readBytes(bytes);
//        Integer res= JSON.parseObject(bytes, Integer.class);
        //System.out.println(id);
        ByteBuf hashCodeBuf = byteBuf.slice(4,byteBuf.readableBytes()).retain();
        //System.out.println(hashCodeBuf.toString(io.netty.util.CharsetUtil.UTF_8));
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1,
                OK, hashCodeBuf);

        //需要加这个吗？
        response.headers().set(CONTENT_TYPE, "text/plain");
        response.headers().set(CONTENT_LENGTH,
                response.content().readableBytes());

        //response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        sendChannel.writeAndFlush(response).addListener(cf->{
            if(!cf.isSuccess()){
                //log.error("send msg to Consumer failed.");
                cf.cause().printStackTrace();
            }
        });
    }

//    private static class Task implements Runnable{
//        private long id;
//        public Task(long id){
//            this.id=id;
//        }
//        @Override
//        public void run() {
//            ChannelHolder.remove(id);
//        }
//    }
}
