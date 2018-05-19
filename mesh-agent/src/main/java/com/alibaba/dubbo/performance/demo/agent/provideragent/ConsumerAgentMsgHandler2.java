package com.alibaba.dubbo.performance.demo.agent.provideragent;

import com.alibaba.dubbo.performance.demo.agent.provideragent.model.RpcFuture;
import com.alibaba.dubbo.performance.demo.agent.provideragent.model.RpcRequest;
import com.alibaba.dubbo.performance.demo.agent.provideragent.model.RpcRequestHolder;
import com.alibaba.dubbo.performance.demo.agent.provideragent.model.RpcResponse;
import com.alibaba.dubbo.performance.demo.agent.provideragent.udp.ProviderAgent;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.CompositeByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.DatagramPacket;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ConsumerAgentMsgHandler2 extends ChannelInboundHandlerAdapter {
    private static ExecutorService threadsPool= Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        threadsPool.submit(new Task(ctx,(DatagramPacket)msg));
    }

    private static class Task implements Runnable{
        private  ChannelHandlerContext ctx;
        private DatagramPacket dp;

        public Task (ChannelHandlerContext ctx,DatagramPacket dp){
            this.ctx=ctx;
            this.dp=dp;
        }
        @Override
        public void run() {
            //fix me:每次做肯定有一定的性能损耗
            ProviderAgent.setMsgReturner(dp.sender());
            ByteBuf buf = dp.content();
            //buf.retain();
            long id=buf.readLong();
            ByteBuf dataBuf=buf.slice(8,buf.readableBytes());
            // dataBuf.retain();
//        System.out.println(id);
//        System.out.println(dataBuf.toString(CharsetUtil.UTF_8));
//        byte[] data=new byte[dataBuf.readableBytes()];
//        dataBuf.readBytes(data);
            RpcRequest request=new RpcRequest(id,dataBuf);
            RpcFuture future = new RpcFuture();
            RpcRequestHolder.put(id,future);
            try {
                ProviderChannelManager.getChannel().write(request);
            }catch (Exception e){
                e.printStackTrace();
            }
            RpcResponse result = null;
            try {
                result = (RpcResponse)future.get();
            }catch (Exception e){
                e.printStackTrace();
            }
            CompositeByteBuf sendBuf= ctx.alloc().compositeDirectBuffer();
            ByteBuf idBuf=ctx.alloc().ioBuffer(8);
            idBuf.writeLong(request.getId());
            sendBuf.addComponents(true,idBuf,result.getBuf());

            DatagramPacket dp = new DatagramPacket(sendBuf,ProviderAgent.getMsgReturner());
            ctx.writeAndFlush(dp);
        }
    }
}
