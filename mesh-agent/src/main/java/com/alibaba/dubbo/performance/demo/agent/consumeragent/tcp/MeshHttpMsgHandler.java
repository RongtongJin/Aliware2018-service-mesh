package com.alibaba.dubbo.performance.demo.agent.consumeragent.tcp;

import com.alibaba.dubbo.performance.demo.agent.holder.CtxHolder;
import com.alibaba.dubbo.performance.demo.agent.loadbalance.RandomLoadBalance;
import com.alibaba.dubbo.performance.demo.agent.utils.EnumKey;

import java.util.concurrent.atomic.AtomicInteger;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;


public final class MeshHttpMsgHandler extends ChannelInboundHandlerAdapter {


    private ChannelHandlerContext outCtx=null;
    private EnumKey type;

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        if(type==EnumKey.S){
            RandomLoadBalance.S_CNT++;
        }
        if(type==EnumKey.M){
            RandomLoadBalance.M_CNT++;
        }
        if(type==EnumKey.L){
            RandomLoadBalance.L_CNT++;
        }
    }

    private int channelId=0;

    private static AtomicInteger genId=new AtomicInteger(0);

    public MeshHttpMsgHandler() {

    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        channelId=genId.getAndIncrement();
        CtxHolder.put(channelId,ctx.pipeline().firstContext());
        TcpChannel tcpChannel=RandomLoadBalance.nextChanelFixed();
        type=tcpChannel.type;
        Channel outCh=tcpChannel.channel;
        outCtx=outCh.pipeline().firstContext();
        if(ctx.channel().eventLoop()!=outCh.eventLoop()){
            ctx.channel().deregister().addListener(future -> ctx.channel().unsafe().register(outCh.eventLoop(),ctx.channel().voidPromise()));
        }
    }


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf byteBuf=(ByteBuf) msg;
        byteBuf.setShort(0,byteBuf.readableBytes()-2);
        byteBuf.setShort(2,channelId);
        outCtx.writeAndFlush(msg,outCtx.voidPromise());
    }


}
