package com.alibaba.dubbo.performance.demo.agent.provideragent.tcp;

import com.alibaba.dubbo.performance.demo.agent.utils.Bytes;
import com.alibaba.dubbo.performance.demo.agent.utils.JsonUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.CompositeByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;
import io.netty.util.ReferenceCountUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

public class TcpConsumerAgentMsgHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        TcpProviderAgent.setConsumerAgentChannel(ctx.channel());
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        TcpProviderChannelManager.getChannel().writeAndFlush(msg);
    }
}
