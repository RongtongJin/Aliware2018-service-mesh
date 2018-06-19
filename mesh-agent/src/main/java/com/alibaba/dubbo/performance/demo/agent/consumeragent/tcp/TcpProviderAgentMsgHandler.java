package com.alibaba.dubbo.performance.demo.agent.consumeragent.tcp;

import com.alibaba.dubbo.performance.demo.agent.holder.CtxHolder;
import com.alibaba.dubbo.performance.demo.agent.holder.bytebuf.TwoCompositeByteBuf;
import com.alibaba.dubbo.performance.demo.agent.utils.ConstUtil;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;


public class TcpProviderAgentMsgHandler extends ChannelInboundHandlerAdapter{

    private ByteBuf headerBuf = null;
    private TwoCompositeByteBuf sendBuf = null;

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        headerBuf = ctx.alloc().directBuffer(ConstUtil.HEADER_LENGTH);
        headerBuf.writeBytes(ConstUtil.template);

        sendBuf = new TwoCompositeByteBuf(ctx.alloc(), headerBuf);
        sendBuf.retain(Integer.MAX_VALUE - 1);
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        headerBuf.release();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf byteBuf=(ByteBuf)msg;
        int channelId =byteBuf.readShort();
        ChannelHandlerContext sendCtx= CtxHolder.get(channelId);
        int len = byteBuf.readableBytes();
        if (len >= 10) {
            headerBuf.setByte(31, ConstUtil.ONE);
            len -= 10;
            headerBuf.setByte(32, ConstUtil.ZERO + len);
        } else {
            headerBuf.setByte(31, ConstUtil.EMP);
            headerBuf.setByte(32, ConstUtil.ZERO + len);
        }
//        CompositeByteBuf sendBuf = ctx.alloc().compositeDirectBuffer();
//        sendBuf.addComponents(true, headerBuf.retain(), byteBuf);
        sendBuf.setHeadAndBody( headerBuf, byteBuf);
        sendCtx.writeAndFlush(sendBuf,sendCtx.voidPromise());
    }

}
