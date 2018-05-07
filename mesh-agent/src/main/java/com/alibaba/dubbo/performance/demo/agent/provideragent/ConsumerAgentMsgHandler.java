package com.alibaba.dubbo.performance.demo.agent.provideragent;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;

/**
 * Created by 79422 on 2018/5/4.
 */
public class ConsumerAgentMsgHandler extends SimpleChannelInboundHandler<DatagramPacket> {

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, DatagramPacket msg) throws Exception {
        System.out.println("-------------------");
        ByteBuf buf = msg.content();
        long id = buf.readLong();
        byte[] bytes = new byte[buf.readableBytes()];
        buf.readBytes(bytes);
        String parameter=new String(bytes);
        System.out.println(id);
        System.out.println(parameter);
        RpcClient.invoke(id,parameter);
    }
}

