package com.alibaba.dubbo.performance.demo.agent.provideragent.udp;


import com.alibaba.dubbo.performance.demo.agent.utils.Bytes;
import com.alibaba.dubbo.performance.demo.agent.utils.JsonUtils;
import io.netty.buffer.ByteBuf;

import io.netty.buffer.CompositeByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;


/**
 * Created by 79422 on 2018/5/4.
 */
public class UdpConsumerAgentMsgHandler extends SimpleChannelInboundHandler<DatagramPacket> {


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket datagramPacket) throws Exception {
        UdpProviderAgent.setMsgReturner(datagramPacket.sender());
        ByteBuf byteBuf = datagramPacket.content().retain();
        UdpProviderChannelManager.getChannel().writeAndFlush(byteBuf);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
//        super.exceptionCaught(ctx, cause);
        cause.printStackTrace();
    }
}

