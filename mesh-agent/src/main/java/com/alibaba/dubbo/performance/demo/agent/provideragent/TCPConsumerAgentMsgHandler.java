package com.alibaba.dubbo.performance.demo.agent.provideragent;

import com.alibaba.dubbo.performance.demo.agent.provideragent.model.ReturnChannelHolder;
import com.alibaba.dubbo.performance.demo.agent.provideragent.model.RpcRequest;
import com.alibaba.dubbo.performance.demo.agent.provideragent.tcp.TCPProviderChannelManager;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TCPConsumerAgentMsgHandler extends SimpleChannelInboundHandler<ByteBuf> {
    private static  final Logger LOGGER= LoggerFactory.getLogger(TCPConsumerAgentMsgHandler.class);
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf byteBuf) throws Exception {

        long id=byteBuf.readLong();
        LOGGER.info("读取comsumer agent的数据"+id);
        byteBuf.retain();
       // System.out.println(id);
//        byte[] bytes = new byte[buf.readableBytes()];
//        buf.readBytes(bytes);
        ByteBuf dataBuf=byteBuf.slice(8,byteBuf.readableBytes());
    //    System.out.println(dataBuf.toString(CharsetUtil.UTF_8));
//        byte [] data=java.lang.String.valueOf(dataBuf.toString(io.netty.util.CharsetUtil.UTF_8).hashCode()).getBytes();
//        ByteBuf sendBuf=ctx.alloc().ioBuffer();
//        sendBuf.writeLong(id);
//        sendBuf.writeBytes(data);
//        System.out.println(id);
        ReturnChannelHolder.put(id,ctx.channel());
        RpcRequest request=new RpcRequest(id,dataBuf);
        TCPProviderChannelManager.getChannel().writeAndFlush(request);
       // System.out.println(sendBuf.toString(CharsetUtil.UTF_8));
        //ctx.writeAndFlush(sendBuf);
    }
}
