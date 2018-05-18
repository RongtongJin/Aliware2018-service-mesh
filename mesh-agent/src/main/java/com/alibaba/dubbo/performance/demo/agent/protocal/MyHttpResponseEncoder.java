package com.alibaba.dubbo.performance.demo.agent.protocal;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;


public class MyHttpResponseEncoder extends MessageToByteEncoder<AgentHttpRequest> {
    public static final byte[] template="HTTP/1.1 200 OK\ncontent-length:".getBytes(); //31
    @Override
    protected void encode(ChannelHandlerContext ctx, AgentHttpRequest msg, ByteBuf byteBuffer) throws Exception {
       // System.err.println("hs"+msg.getHash());
        byteBuffer.writeBytes(template);
        String hash=String.valueOf(msg.getHash());
        byteBuffer.writeBytes((hash.length()+"\n\n"+hash).getBytes());
//        int hashCode=msg.getHash();
//        byteBuffer.writeBytes(template);
//        if(hashCode==-2147483648){
//            byteBuffer.writeBytes("11\n\n-2147483648".getBytes());
//        }
//        ByteBuffer tmp=ByteBuffer.allocate(64);
//        tmp.clear();
//        int start=0;
//        if(hashCode<0){
//            tmp.put(ConstUtil.N);
//            hashCode=0-hashCode;
//            start=1;
//        }
//        while(hashCode!=0){
//            tmp.put((byte)(hashCode%10+ ConstUtil.ZERO));
//            hashCode/=10;
//        }
//        int cnt=tmp.position();
//        if(cnt<10){
//            byteBuffer.writeByte((byte)(cnt+ ConstUtil.ZERO));
//        }else{
//            byteBuffer.writeByte(ConstUtil.ONE);
//            byteBuffer.writeByte((byte)(cnt-10+ ConstUtil.ZERO));
//        }
//        byteBuffer.writeByte(ConstUtil.SEP);
//        byteBuffer.writeByte(ConstUtil.SEP);
//        if(start==1){
//            byteBuffer.writeByte(ConstUtil.N);
//        }
//        for(int i=cnt-1;i>=start;i--){
//            byteBuffer.writeByte(tmp.get(i));
//        }

    }
}
