package com.alibaba.dubbo.performance.demo.agent.provideragent;

import com.alibaba.dubbo.performance.demo.agent.provideragent.model.RpcResponse;
import com.alibaba.fastjson.JSON;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.CompositeByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import io.netty.util.CharsetUtil;

import java.net.InetSocketAddress;

/**
 * Created by 79422 on 2018/5/4.
 */
public class RpcMsgHandler extends SimpleChannelInboundHandler<ByteBuf> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf byteBuf) throws Exception {
        byteBuf.retain();
        ByteBuf idBuf=byteBuf.slice(0,8);
        byteBuf.readerIndex(13);
        byte[] res=new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(res);
        //System.out.println(new String(res));
        CompositeByteBuf sendBuf= ctx.alloc().compositeBuffer();
        ByteBuf hashCodeBuf=ctx.alloc().ioBuffer();
        hashCodeBuf.writeBytes(JSON.parseObject(res,Integer.class).toString().getBytes());
        sendBuf.addComponents(true,idBuf,hashCodeBuf);
        //这边的ip地址可能有问题
        DatagramPacket dp = new DatagramPacket(sendBuf,ProviderAgent.getMsgReturner());
        //System.out.println(ProviderAgent.getMsgReturner().getHostString()+":"+ProviderAgent.getMsgReturner().getPort());
        ProviderAgent.getUDPChannel().write(dp).addListener(cf->{
            if(!cf.isSuccess()){
                System.err.println("err in back to consumer agent");
                cf.cause().printStackTrace();
            }
        });
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ProviderAgent.getUDPChannel().flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
    }
}
