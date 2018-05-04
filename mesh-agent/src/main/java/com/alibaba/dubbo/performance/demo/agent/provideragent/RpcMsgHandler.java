package com.alibaba.dubbo.performance.demo.agent.provideragent;

import com.alibaba.dubbo.performance.demo.agent.provideragent.rpcmodel.RpcResponse;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;

import java.net.InetSocketAddress;

/**
 * Created by 79422 on 2018/5/4.
 */
public class RpcMsgHandler extends SimpleChannelInboundHandler<RpcResponse> {

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcResponse response) throws Exception {
        long requestId = response.getRequestId();
        //System.out.println("requestId="+requestId);
        //System.out.println(new String(response.getBytes()));
        ByteBuf byteBuf= Unpooled.directBuffer(response.getBytes().length+8);
        byteBuf.writeLong(requestId);
        byteBuf.writeBytes(response.getBytes());
        DatagramPacket dp = new DatagramPacket(byteBuf,new InetSocketAddress("127.0.0.1",20000));
        ProviderAgent.getUDPChannel().writeAndFlush(dp).addListener(cf->{
            if(!cf.isSuccess()){
                System.err.println("err in back to consumer agent");
                cf.cause().printStackTrace();
            }
        });
    }
}
