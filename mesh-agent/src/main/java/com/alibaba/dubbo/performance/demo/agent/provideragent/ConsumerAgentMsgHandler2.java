package com.alibaba.dubbo.performance.demo.agent.provideragent;

import com.alibaba.dubbo.performance.demo.agent.utils.Bytes;
import com.alibaba.dubbo.performance.demo.agent.utils.JsonUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.CompositeByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.DatagramPacket;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ConsumerAgentMsgHandler2 extends ChannelInboundHandlerAdapter {
    private static ExecutorService threadsPool= Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    // header length.
    protected static final int HEADER_LENGTH = 16;
    // magic header.
    protected static final short MAGIC = (short) 0xdabb;
    // message flag.
    protected static final byte FLAG_REQUEST = (byte) 0x80;
    protected static final byte FLAG_TWOWAY = (byte) 0x40;

    private static ByteBuf headerBuf=null;

    private static ByteBuf frontBody=null;

    private static ByteBuf tailBody=null;

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        headerBuf=ctx.alloc().buffer(HEADER_LENGTH);
        frontBody=ctx.alloc().buffer();
        tailBody=ctx.alloc().buffer();

        byte[] header = new byte[HEADER_LENGTH];
        //header
        Bytes.short2bytes(MAGIC, header);
        // set request and serialization flag.
        header[2] = (byte) (FLAG_REQUEST | 6);
        header[2] |= FLAG_TWOWAY;

        headerBuf.writeBytes(header);

        //front body
        ByteArrayOutputStream frontOut = new ByteArrayOutputStream();
        PrintWriter frontWriter = new PrintWriter(new OutputStreamWriter(frontOut));

        try{
            //Dubbo version
            JsonUtils.writeObject("2.0.1",frontWriter);
            //Service name
            JsonUtils.writeObject("com.alibaba.dubbo.performance.demo.provider.IHelloService",frontWriter);
            //Service version
            JsonUtils.writeObject(null,frontWriter);
            //Method name
            JsonUtils.writeObject("hash",frontWriter);
            //Method parameter types
            JsonUtils.writeObject("Ljava/lang/String;",frontWriter);
        }catch (IOException e){
            e.printStackTrace();
        }

        frontBody.writeBytes(frontOut.toByteArray());
        frontBody.writeByte('"');

        //tailBody
        ByteArrayOutputStream tailOut = new ByteArrayOutputStream();
        PrintWriter tailWriter = new PrintWriter(new OutputStreamWriter(tailOut));

        Map<String,String> map=new HashMap<>();
        map.put("path","com.alibaba.dubbo.performance.demo.provider.IHelloService");

        try{
            JsonUtils.writeObject(map,tailWriter);
        }catch (IOException e){
            e.printStackTrace();
        }

        tailBody.writeByte('"');
        tailBody.writeBytes(System.lineSeparator().getBytes());
        tailBody.writeBytes(tailOut.toByteArray());

    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        headerBuf.release();
        frontBody.release();
        tailBody.release();
    }
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
            ProviderAgent.setMsgReturner(dp.sender());
            ByteBuf buf = dp.content();
            long id=buf.readLong();
            ByteBuf dataBuf=buf.slice(8,buf.readableBytes());
            int bodyLen=frontBody.readableBytes()+dataBuf.readableBytes()+tailBody.readableBytes();
            ByteBuf headerDup=headerBuf.copy();
            int saveWriterIndex=headerDup.writerIndex();
            headerDup.writerIndex(4);
            headerDup.writeLong(id);
            headerDup.writeInt(bodyLen);
            headerDup.writerIndex(saveWriterIndex);
            CompositeByteBuf sendBuf=ctx.alloc().compositeDirectBuffer();
            sendBuf.addComponents(true,headerDup,frontBody.copy(),dataBuf,tailBody.copy());
            try {
                ProviderChannelManager.getChannel().writeAndFlush(sendBuf);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
}
