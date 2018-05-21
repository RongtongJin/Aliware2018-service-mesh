package com.alibaba.dubbo.performance.demo.agent.provideragent.udp;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.CompositeByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;

import java.net.InetSocketAddress;

/**
 * Created by 79422 on 2018/5/4.
 */
public class UdpRpcMsgHandler extends SimpleChannelInboundHandler<ByteBuf> {

    protected static final byte FLAG_EVENT = (byte) 0x20;
    private static final int len=System.lineSeparator().length();

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf byteBuf) throws Exception {

        byte byte2=byteBuf.getByte(2);
        byte status=byteBuf.getByte(3);
        if((byte2&FLAG_EVENT)==0&&status==20){
            ByteBuf idBuf=byteBuf.slice(8,4).retain();
            ByteBuf hashCodeBuf = byteBuf.slice(17 + len, byteBuf.readableBytes() - 17 - 2 * len).retain();
            CompositeByteBuf sendBuf= ctx.alloc().compositeDirectBuffer();
            sendBuf.addComponents(true,idBuf,hashCodeBuf);
            DatagramPacket dp=new DatagramPacket(sendBuf,UdpProviderAgent.getMsgReturner());
            UdpProviderAgent.getUdpChannel().writeAndFlush(dp);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
    }
}
