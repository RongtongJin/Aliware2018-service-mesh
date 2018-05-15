package com.alibaba.dubbo.performance.demo.agent.provideragent;

import com.alibaba.dubbo.performance.demo.agent.provideragent.model.RpcResponse;
import com.alibaba.fastjson.JSON;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
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

    protected static final byte FLAG_EVENT = (byte) 0x20;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf byteBuf) throws Exception {

        byte byte2=byteBuf.getByte(2);
        byte status=byteBuf.getByte(3);
        if((byte2&FLAG_EVENT)==0&&status==20){
            byteBuf.retain();
            ByteBuf idBuf=byteBuf.slice(4,8);
            ByteBuf hashCodeBuf=byteBuf.slice(19,byteBuf.readableBytes()-21);
            System.out.println("---------------------");
//            byteBuf.readerIndex(17);
//            byte[] res=new byte[byteBuf.readableBytes()];
//            byteBuf.readBytes(res);
//            ByteBuf hashCodeBuf=ctx.alloc().ioBuffer();
//            hashCodeBuf.writeBytes(JSON.parseObject(res,Integer.class).toString().getBytes());
            CompositeByteBuf sendBuf= ctx.alloc().compositeDirectBuffer();
            sendBuf.addComponents(true,idBuf,hashCodeBuf);

            DatagramPacket dp = new DatagramPacket(sendBuf,ProviderAgent.getMsgReturner());
            //System.out.println(ProviderAgent.getMsgReturner().getHostString()+":"+ProviderAgent.getMsgReturner().getPort());
            ProviderAgent.getUDPChannel().write(dp).addListener(cf->{
                if(!cf.isSuccess()){
                    System.err.println("err in back to consumer agent");
                    cf.cause().printStackTrace();
                }
            });
        }
    }

//   @Override
//    protected void channelRead0(ChannelHandlerContext ctx, RpcResponse response) throws Exception {
//
//        ByteBuf sendBuf= ctx.alloc().ioBuffer();
//        sendBuf.writeLong(response.getRequestId());
//        sendBuf.writeBytes(response.getBytes());
//        //这边的ip地址可能有问题
//        DatagramPacket dp = new DatagramPacket(sendBuf,ProviderAgent.getMsgReturner());
//        //System.out.println(ProviderAgent.getMsgReturner().getHostString()+":"+ProviderAgent.getMsgReturner().getPort());
//        ProviderAgent.getUDPChannel().write(dp).addListener(cf->{
//            if(!cf.isSuccess()){
//                System.err.println("err in back to consumer agent");
//                cf.cause().printStackTrace();
//            }
//        });
//    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ProviderAgent.getUDPChannel().flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
    }
}
