package com.alibaba.dubbo.performance.demo.agent.protocal;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;


public final class AgentHttpRequest {
    public int len;
    public ByteBuf bodyBuf;
    public AgentHttpRequest(ByteBuf byteBuffer, ChannelHandlerContext ctx){
        len=byteBuffer.readableBytes()+8;
        this.bodyBuf.clear();
        this.bodyBuf.writeInt(len);
        this.bodyBuf.writeInt(ctx.hashCode());
        this.bodyBuf.writeBytes(byteBuffer);
    }
    AgentHttpRequest(){
        bodyBuf = Unpooled.directBuffer(2048);
    }
    AgentHttpRequest(ByteBuf bodyBuf){
        this.bodyBuf=bodyBuf;
    }
    AgentHttpRequest setBody(ByteBuf byteBuffer, ChannelHandlerContext ctx){
        this.bodyBuf.clear();
        len=byteBuffer.readableBytes()+8;
        this.bodyBuf.writeInt(len);
        this.bodyBuf.writeInt(ctx.hashCode());
        this.bodyBuf.writeBytes(byteBuffer);
        return this;
    }
    public static void main(String[] args) {
//        String str="interface=com.alibaba.dubbo.performance.demo.provider.IHelloService&method=hash&parameterTypesString=Ljava%2Flang%2FString%3B&parameter=lsx";
//        ByteBuffer byteBuffer=ByteBuffer.wrap(str.getBytes());
//        AgentHttpRequest agentHttpPacket=new AgentHttpRequest(byteBuffer);
//        try {
//            agentHttpPacket.getHash();
//        }catch (Exception e){
//
//        }

    }
    public int getHash() throws Exception {
        int i=len-1;
        while(i>=8&&this.bodyBuf.getByte(i)!='='){
            i--;
        }
        if(i<8){
            System.err.println("获取body的hash失败!");
            return -1;
        }else{
            byte[] byteBuffer=new byte[len-1-i];
            int j=0;
            while(++i<len){
                byteBuffer[j++]=this.bodyBuf.getByte(i);
            }
            String str=new String(byteBuffer);
            //System.err.println(str);
            return str.hashCode();
        }
    }
}
