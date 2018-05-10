package com.alibaba.dubbo.performance.demo.agent.provideragent;

import com.alibaba.dubbo.performance.demo.agent.provideragent.rpcmodel.RpcRequest;
import com.sun.org.apache.xpath.internal.operations.String;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import io.netty.util.CharsetUtil;

/**
 * Created by 79422 on 2018/5/4.
 */
public class ConsumerAgentMsgHandler extends SimpleChannelInboundHandler<DatagramPacket> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket msg) throws Exception {
        //System.out.println("----------------");
        ByteBuf buf = msg.content();
       // System.out.println(buf.toString(CharsetUtil.UTF_8));
        long id=buf.readLong();
//        byte[] bytes = new byte[buf.readableBytes()];
//        buf.readBytes(bytes);
        ByteBuf dataBuf=buf.slice(8,buf.readableBytes());
       // System.out.println(dataBuf.toString(io.netty.util.CharsetUtil.UTF_8));
        byte [] data=java.lang.String.valueOf(dataBuf.toString(io.netty.util.CharsetUtil.UTF_8).hashCode()).getBytes();
        ByteBuf sendBuf=ctx.alloc().ioBuffer(8+data.length);
        sendBuf.writeLong(id);
        sendBuf.writeBytes(data);
        DatagramPacket dp=new DatagramPacket(Unpooled.wrappedBuffer(sendBuf),msg.sender());
       // System.out.println(msg.sender().getHostString());
       // System.out.println(msg.sender().getPort());
        ctx.writeAndFlush(dp);
//        RpcRequest request=new RpcRequest(id,bytes);
//        ProviderChannelManager.getChannel().write(request);
    }

}

