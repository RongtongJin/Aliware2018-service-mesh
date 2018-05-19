package com.alibaba.dubbo.performance.demo.agent.provideragent;

import com.alibaba.dubbo.performance.demo.agent.provideragent.model.RpcRequest;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.SimpleChannelInboundHandler;

public class TCPConsumerAgentMsgHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        TCPProviderAgent.setConsumerAgentChannel(ctx.channel());
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

//        ByteBuf idBuf=byteBuf.slice(0,8).retain();
//        ByteBuf dataBuf=byteBuf.slice(8,byteBuf.readableBytes()-8).retain();
//        System.out.println(dataBuf.toString(CharsetUtil.UTF_8));
//        byte [] data=java.lang.String.valueOf(dataBuf.toString(io.netty.util.CharsetUtil.UTF_8).hashCode()).getBytes();
//        ByteBuf sendBuf=ctx.alloc().ioBuffer();
//        sendBuf.writeLong(id);
//        sendBuf.writeBytes(data);
//        System.out.println(id);
//        ReturnChannelHolder.put(id,ctx.channel());
//        RpcRequest request=new RpcRequest(idBuf,dataBuf);
        //TCPProviderAgent.getChannelGroup().nextChannel().writeAndFlush(request);
        TCPProviderChannelManager.getChannel().writeAndFlush(msg);
       // System.out.println(sendBuf.toString(CharsetUtil.UTF_8));
        //ctx.writeAndFlush(sendBuf);
    }
}
