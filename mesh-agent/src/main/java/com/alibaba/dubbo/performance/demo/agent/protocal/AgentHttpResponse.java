package com.alibaba.dubbo.performance.demo.agent.protocal;

import com.alibaba.dubbo.performance.demo.agent.utils.ConstUtils;
import com.sun.xml.internal.bind.v2.runtime.reflect.opt.Const;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.nio.ByteBuffer;


public class AgentHttpResponse {
    //    public static final byte[] template=("HTTP/1.1 200 OK\n" +
//            "content-length:11\n" +
//            "\n" +
//            "-1038596628").getBytes();
    public static final byte[] template="HTTP/1.1 200 OK\ncontent-length:".getBytes(); //31
    public ByteBuf byteBuffer= Unpooled.buffer(64);
    public ByteBuffer tmp=ByteBuffer.allocate(64);
    public AgentHttpResponse(){
        byteBuffer.clear();
        tmp.clear();
        byteBuffer.writeBytes(template);
        //byteBuffer.writerIndex(31);
        //byteBuffer.writeBytes(template);
    }

    public static void main(String[] args) {
        AgentHttpResponse agentHttpResponse= new AgentHttpResponse();
        agentHttpResponse.setBody(-38596628);
        System.err.println(agentHttpResponse);
        System.err.println(agentHttpResponse.byteBuffer);
        //int a=2147483648;
    }
    public AgentHttpResponse setBody(int hashCode){
        //byteBuffer.readableBytes(31);
        if(hashCode==-2147483648){
            byteBuffer.writeBytes("11\n\n-2147483648".getBytes());
            return this;
        }
        tmp.clear();
        int start=0;
        if(hashCode<0){
            tmp.put(ConstUtils.N);
            hashCode=0-hashCode;
            start=1;
        }
        while(hashCode!=0){
            tmp.put((byte)(hashCode%10+ ConstUtils.ZERO));
            hashCode/=10;
        }
        int cnt=tmp.position();
        if(cnt<10){
            byteBuffer.writeByte((byte)(cnt+ ConstUtils.ZERO));
        }else{
            byteBuffer.writeByte(ConstUtils.ONE);
            byteBuffer.writeByte((byte)(cnt-10+ ConstUtils.ZERO));
        }
        byteBuffer.writeByte(ConstUtils.SEP);
        byteBuffer.writeByte(ConstUtils.SEP);
        if(start==1){
            byteBuffer.writeByte(ConstUtils.N);
        }
        for(int i=cnt-1;i>=start;i--){
            byteBuffer.writeByte(tmp.get(i));
        }
        return this;
    }

    @Override
    public String toString() {
        return new String(byteBuffer.array(),0,byteBuffer.readableBytes());
    }
}
