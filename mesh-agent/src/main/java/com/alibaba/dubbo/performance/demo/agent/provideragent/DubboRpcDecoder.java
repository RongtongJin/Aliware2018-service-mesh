package com.alibaba.dubbo.performance.demo.agent.provideragent;

import com.alibaba.dubbo.performance.demo.agent.utils.Bytes;
import com.alibaba.dubbo.performance.demo.agent.provideragent.rpcmodel.RpcResponse;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.Arrays;
import java.util.List;

public class DubboRpcDecoder extends ByteToMessageDecoder {
    // header length.
    protected static final int HEADER_LENGTH = 16;

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) {
       list.add(decode2(byteBuf));
    }

    private Object decode2(ByteBuf byteBuf){
        System.out.println("decode");
        byte[] data = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(data);

        byte[] subArray = Arrays.copyOfRange(data,HEADER_LENGTH + 1, data.length);

        String s = new String(subArray);

        byte[] requestIdBytes = Arrays.copyOfRange(data,4,12);
        long requestId = Bytes.bytes2long(requestIdBytes,0);

        //System.out.println("get from dubbo requestId"+requestId);

        RpcResponse response = new RpcResponse();
        response.setRequestId(requestId);
        response.setBytes(subArray);
        return response;
    }
}
