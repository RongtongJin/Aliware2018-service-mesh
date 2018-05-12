package com.alibaba.dubbo.performance.demo.agent.provideragent;

import com.alibaba.dubbo.performance.demo.agent.provideragent.model.RpcResponse;
import com.alibaba.fastjson.JSON;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;

import java.net.InetSocketAddress;

/**
 * Created by 79422 on 2018/5/4.
 */
public class RpcMsgHandler extends SimpleChannelInboundHandler<RpcResponse> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcResponse response) throws Exception {
        long requestId = response.getRequestId();
        ByteBuf byteBuf= ctx.alloc().buffer();
        byteBuf.writeLong(requestId);
        byteBuf.writeBytes(JSON.parseObject(response.getBytes(),Integer.class).toString().getBytes());
        //这边的ip地址可能有问题
        DatagramPacket dp = new DatagramPacket(byteBuf,ProviderAgent.getMsgReturner());
        //System.out.println(ProviderAgent.getMsgReturner().getHostString()+":"+ProviderAgent.getMsgReturner().getPort());
        ProviderAgent.getUDPChannel().writeAndFlush(dp).addListener(cf->{
            if(!cf.isSuccess()){
                System.err.println("err in back to consumer agent");
                cf.cause().printStackTrace();
            }
        });
    }
}
