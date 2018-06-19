package com.alibaba.dubbo.performance.demo.agent.provideragent.tcp;


import com.alibaba.dubbo.performance.demo.agent.holder.RequestId2ChannelId;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class TcpRpcMsgHandler extends SimpleChannelInboundHandler<ByteBuf> {
    private static final int len=System.lineSeparator().length();

    private static ChannelHandlerContext outCtx=null;

    public static final void setOutCtx(Channel ch){
        outCtx=ch.pipeline().firstContext();
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, ByteBuf byteBuf) throws Exception {
//        byte status=byteBuf.getByte(3);
//        if(status==20){
        int requestId = byteBuf.getInt(8);
        int channelId = RequestId2ChannelId.get(requestId);
        ByteBuf sendBuf = byteBuf.slice(14 + len, byteBuf.readableBytes() - 14 - 2 * len).retain();
        sendBuf.setByte(0, sendBuf.readableBytes() - 1);
        sendBuf.setShort(1, channelId);
        outCtx.writeAndFlush(sendBuf,outCtx.voidPromise());
//        }
//        else if (status==100){
//            System.out.println("error code: "+status);
//            int requestId = byteBuf.getInt(8);
//            int channelId = RequestId2ChannelId.get(requestId);
//            ByteBuf sendBuf= byteBuf.slice(0,6).retain();
//            sendBuf.writerIndex(3);
//            sendBuf.setByte(0, sendBuf.readableBytes() - 1);
//            sendBuf.setInt(1, channelId);
//            outCtx.writeAndFlush(sendBuf,outCtx.voidPromise());
//        }
    }
}
