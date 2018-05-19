package com.alibaba.dubbo.performance.demo.agent.provideragent;

import com.alibaba.dubbo.performance.demo.agent.provideragent.model.RpcRequest;
import com.alibaba.dubbo.performance.demo.agent.utils.Bytes;
import com.alibaba.dubbo.performance.demo.agent.utils.JsonUtils;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.util.CharsetUtil;

import java.io.*;
import java.util.HashMap;
import java.util.Map;


//利用零拷贝技术仍然可以改进
public class DubboRpcEncoder extends MessageToByteEncoder{
    // header length.
    protected static final int HEADER_LENGTH = 16;
    // magic header.
    protected static final short MAGIC = (short) 0xdabb;
    // message flag.
    protected static final byte FLAG_REQUEST = (byte) 0x80;
    protected static final byte FLAG_TWOWAY = (byte) 0x40;

    private static byte[] header = new byte[HEADER_LENGTH];

    private static byte[] frontBody ;

    private static byte[] tailBody ;

    static {
        //header
        Bytes.short2bytes(MAGIC, header);
        // set request and serialization flag.
        header[2] = (byte) (FLAG_REQUEST | 6);
        header[2] |= FLAG_TWOWAY;

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
        frontBody=frontOut.toByteArray();

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
        tailBody=tailOut.toByteArray();
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf buffer) throws Exception {
        RpcRequest req = (RpcRequest)msg;
        int dataLen=req.getParameter().readableBytes()+2+System.lineSeparator().length();
        int savedWriteIndex = buffer.writerIndex();
        buffer.writeBytes(header);
        buffer.writeBytes(frontBody);
        buffer.writeByte('"');
        buffer.writeBytes(req.getParameter());
        buffer.writeByte('"');
        buffer.writeBytes(System.lineSeparator().getBytes());
        buffer.writeBytes(tailBody);
        buffer.writerIndex(savedWriteIndex+4);
        //RPC request ID
        buffer.writeBytes(req.getId());
        int len=frontBody.length+dataLen+tailBody.length;
        //body length
        buffer.writeInt(len);
        buffer.writerIndex(savedWriteIndex + HEADER_LENGTH + len);
        req.getParameter().release();
    }

}
