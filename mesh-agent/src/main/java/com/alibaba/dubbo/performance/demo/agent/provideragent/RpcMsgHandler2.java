package com.alibaba.dubbo.performance.demo.agent.provideragent;

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

    private static ExecutorService threadsPool= Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object buf) throws Exception {
        threadsPool.submit(new Task(ctx,(ByteBuf) buf));
    }

    private static class Task implements Runnable{

        private  ChannelHandlerContext ctx;
        private ByteBuf byteBuf;

        public Task (ChannelHandlerContext ctx,ByteBuf byteBuf){
            this.ctx=ctx;
            this.byteBuf=byteBuf;
        }
        @Override
        public void run() {
            byte byte2=byteBuf.getByte(2);
            byte status=byteBuf.getByte(3);
            if((byte2&FLAG_EVENT)==0&&status==20){
                byteBuf.retain();
                int len=System.lineSeparator().length();
                ByteBuf idBuf=byteBuf.slice(4,8);
                ByteBuf hashCodeBuf=byteBuf.slice(17+len,byteBuf.readableBytes()-17-2*len);
                System.out.println("---------------------");
                System.out.println(idBuf.toString(CharsetUtil.UTF_8));
                System.out.println(hashCodeBuf.toString(CharsetUtil.UTF_8));
//            byteBuf.readerIndex(17);
//            byte[] res=new byte[byteBuf.readableBytes()];
//            byteBuf.readBytes(res);
//            ByteBuf hashCodeBuf=ctx.alloc().ioBuffer();
//            hashCodeBuf.writeBytes(JSON.parseObject(res,Integer.class).toString().getBytes());
                CompositeByteBuf sendBuf= ctx.alloc().compositeDirectBuffer();
                ByteBuf id=ctx.alloc().ioBuffer(8);
                id.writeBytes(idBuf);
                sendBuf.addComponents(true,id,hashCodeBuf);

                DatagramPacket dp = new DatagramPacket(sendBuf,ProviderAgent.getMsgReturner());
                //System.out.println(ProviderAgent.getMsgReturner().getHostString()+":"+ProviderAgent.getMsgReturner().getPort());
                ProviderAgent.getUDPChannel().write(dp).addListener(cf->{
                    if(!cf.isSuccess()){
                        System.err.println("err in back to consumer agent");
                        cf.cause().printStackTrace();
                    }
                });
            }else{
                byteBuf.release();
            }
        }
    }
}
