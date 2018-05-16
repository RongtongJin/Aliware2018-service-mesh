package com.alibaba.dubbo.performance.demo.agent.provideragent;

import com.alibaba.dubbo.performance.demo.agent.provideragent.model.RpcFuture;
import com.alibaba.dubbo.performance.demo.agent.provideragent.model.RpcRequestHolder;
import com.alibaba.dubbo.performance.demo.agent.provideragent.model.RpcResponse;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.CompositeByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.DatagramPacket;
import io.netty.util.CharsetUtil;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class RpcMsgHandler2 extends ChannelInboundHandlerAdapter {

    protected static final byte FLAG_EVENT = (byte) 0x20;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf byteBuf=(ByteBuf)msg;
        byte byte2=byteBuf.getByte(2);
        byte status=byteBuf.getByte(3);
        if((byte2&FLAG_EVENT)==0&&status==20) {
            byteBuf.retain();
            int len = System.lineSeparator().length();
            long id=byteBuf.getLong(4);
            ByteBuf hashCodeBuf = byteBuf.slice(17 + len, byteBuf.readableBytes() - 17 - 2 * len);
            RpcFuture future= RpcRequestHolder.get(id);
            RpcResponse res=new RpcResponse(id,hashCodeBuf);
            res.setBuf(hashCodeBuf);
            if(null!=future){
                RpcRequestHolder.remove(id);
                future.done(res);
            }
        }
    }
}
