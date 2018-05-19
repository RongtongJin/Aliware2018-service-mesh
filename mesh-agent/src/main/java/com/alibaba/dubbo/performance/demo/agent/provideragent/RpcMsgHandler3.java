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
import io.netty.channel.SimpleChannelInboundHandler;

public class RpcMsgHandler3  extends SimpleChannelInboundHandler<ByteBuf> {
    protected static final byte FLAG_EVENT = (byte) 0x20;

    @Override
    public void channelRead0(ChannelHandlerContext ctx, ByteBuf byteBuf) throws Exception {

        byte byte2=byteBuf.getByte(2);
        byte status=byteBuf.getByte(3);
        if((byte2&FLAG_EVENT)==0&&status==20) {
            int len = System.lineSeparator().length();
            ByteBuf idBuf=byteBuf.slice(4,8).retain();
            ByteBuf hashCodeBuf = byteBuf.slice(17 + len, byteBuf.readableBytes() - 17 - 2 * len).retain();
            CompositeByteBuf sendBuf= ctx.alloc().compositeDirectBuffer();
            sendBuf.addComponents(true,idBuf,hashCodeBuf);
            TCPProviderAgent.getConsumerAgentChannel().writeAndFlush(sendBuf);
        }
    }
}
