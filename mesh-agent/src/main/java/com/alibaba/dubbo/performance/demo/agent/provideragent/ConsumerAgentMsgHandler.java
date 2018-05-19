package com.alibaba.dubbo.performance.demo.agent.provideragent;


import com.alibaba.dubbo.performance.demo.agent.provideragent.model.RpcRequest;

import io.netty.buffer.ByteBuf;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import io.netty.util.CharsetUtil;

import java.security.Provider;


/**
 * Created by 79422 on 2018/5/4.
 */
public class ConsumerAgentMsgHandler extends SimpleChannelInboundHandler<DatagramPacket> {

    @Override
    public void channelRead0(ChannelHandlerContext ctx, DatagramPacket msg) throws Exception {
        //fix me:每次做肯定有一定的性能损耗
        ProviderAgent.setMsgReturner(msg.sender());
        ByteBuf byteBuf = msg.content();
        //buf.retain();
        ByteBuf idBuf=byteBuf.slice(0,8).retain();
        ByteBuf dataBuf=byteBuf.slice(8,byteBuf.readableBytes()-8).retain();
       // dataBuf.retain();
//        System.out.println(id);
//        System.out.println(dataBuf.toString(CharsetUtil.UTF_8));
//        byte[] data=new byte[dataBuf.readableBytes()];
//        dataBuf.readBytes(data);
        RpcRequest request=new RpcRequest(idBuf,dataBuf);


        ProviderChannelManager.getChannel().writeAndFlush(request);

        /*用于验证不经过provider性能*/
//        byte [] data=java.lang.String.valueOf(dataBuf.toString(io.netty.util.CharsetUtil.UTF_8).hashCode()).getBytes();
//        ByteBuf sendBuf=ctx.alloc().ioBuffer(8+data.length);
//        sendBuf.writeLong(id);
//        sendBuf.writeBytes(data);
//        DatagramPacket dp=new DatagramPacket(sendBuf,msg.sender());
//        ctx.writeAndFlush(dp);
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
//        super.exceptionCaught(ctx, cause);
        cause.printStackTrace();
    }
}

