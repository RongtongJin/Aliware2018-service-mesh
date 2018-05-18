package com.alibaba.dubbo.performance.demo.agent.provideragent;

import com.alibaba.dubbo.performance.demo.agent.provideragent.model.RpcRequest;
import com.alibaba.dubbo.performance.demo.agent.utils.BytesUtil;
import com.alibaba.dubbo.performance.demo.agent.utils.JsonUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.CompositeByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

public class DubboRpcEncoder2 extends ChannelOutboundHandlerAdapter {
    // header length.
    protected static final int HEADER_LENGTH = 16;
    // magic header.
    protected static final short MAGIC = (short) 0xdabb;
    // message flag.
    protected static final byte FLAG_REQUEST = (byte) 0x80;
    protected static final byte FLAG_TWOWAY = (byte) 0x40;

    private ByteBuf headerBuf=null;

    private ByteBuf frontBody=null;

    private ByteBuf tailBody=null;

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        headerBuf=ctx.alloc().buffer(HEADER_LENGTH);
        frontBody=ctx.alloc().buffer();
        tailBody=ctx.alloc().buffer();

        byte[] header = new byte[HEADER_LENGTH];
        //header
        BytesUtil.short2bytes(MAGIC, header);
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
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        RpcRequest req = (RpcRequest)msg;
//        System.out.println(req.getId());
//        System.out.println(req.getParameter().toString(CharsetUtil.UTF_8));
        int bodyLen=frontBody.readableBytes()+req.getParameter().readableBytes()+tailBody.readableBytes();
        ByteBuf headerDup=headerBuf.copy();
        int saveWriterIndex=headerDup.writerIndex();
        headerDup.writerIndex(4);
        headerDup.writeLong(req.getId());
        headerDup.writeInt(bodyLen);
        headerDup.writerIndex(saveWriterIndex);
        CompositeByteBuf sendBuf=ctx.alloc().compositeDirectBuffer();
        sendBuf.addComponents(true,headerDup,frontBody.copy(),req.getParameter(),tailBody.copy());
        ctx.writeAndFlush(sendBuf,promise);
    }
}
