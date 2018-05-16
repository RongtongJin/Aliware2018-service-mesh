package com.alibaba.dubbo.performance.demo.agent.provideragent;

import com.alibaba.dubbo.performance.demo.agent.provideragent.model.ReturnChannelHolder;
import com.alibaba.dubbo.performance.demo.agent.provideragent.model.RpcFuture;
import com.alibaba.dubbo.performance.demo.agent.provideragent.model.RpcRequestHolder;
import com.alibaba.dubbo.performance.demo.agent.provideragent.model.RpcResponse;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.CompositeByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class RpcMsgHandler3  extends ChannelInboundHandlerAdapter {
    protected static final byte FLAG_EVENT = (byte) 0x20;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf byteBuf=(ByteBuf)msg;
        byte byte2=byteBuf.getByte(2);
        byte status=byteBuf.getByte(3);
        if((byte2&FLAG_EVENT)==0&&status==20) {
            int len = System.lineSeparator().length();
            long id=byteBuf.getLong(4);
            ByteBuf hashCodeBuf = byteBuf.slice(17 + len, byteBuf.readableBytes() - 17 - 2 * len);
            Channel ch=ReturnChannelHolder.get(id);
            ReturnChannelHolder.remove(id);
            CompositeByteBuf sendBuf= ctx.alloc().compositeDirectBuffer();
            ByteBuf idBuf=ctx.alloc().ioBuffer(8);
            idBuf.writeLong(id);
            sendBuf.addComponents(true,idBuf,hashCodeBuf);
            ch.writeAndFlush(sendBuf);
        }else{
            byteBuf.release();
        }
    }
}
