package com.alibaba.dubbo.performance.demo.agent.consumeragent.common;

import com.alibaba.dubbo.performance.demo.agent.utils.ConstUtil;

import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

public final class MeshHttpRequestDecoder extends ByteToMessageDecoder {

    public enum Step {
        NEW,
       BODY
    }

    private Step state = Step.NEW;
    private int lastReadIndex = 0;
    private int contentLen = 0;


    protected void decode(ChannelHandlerContext ctx, ByteBuf buffer, List<Object> out) throws Exception {

        if (buffer.readableBytes() < ConstUtil.MIN_LEN) {
            return; //数据,头部143+body至少136,
        }
        if (state == Step.NEW) {

            buffer.readerIndex(33);

            contentLen = 0;
            byte b;
            while ((b = buffer.readByte()) != ConstUtil.CR) {
                contentLen = contentLen * 10 + (b - ConstUtil.ZERO);
            }
            state = Step.BODY;
            lastReadIndex =buffer.readerIndex()+ConstUtil.SKIP_HEAD;
        }
        if (state == Step.BODY) {
            buffer.readerIndex(lastReadIndex);
            if (buffer.readableBytes() == contentLen) {
//                buffer.skipBytes(136);
                int index = buffer.readerIndex();
                int len = buffer.readableBytes();

                ByteBuf slice = buffer.slice(index-4, len+4);
                buffer.readerIndex(index + len);

                out.add(slice.retain());
                state = Step.NEW;
                lastReadIndex = 0;
                contentLen = 0;
                return;
            }
//            else if (buffer.readableBytes() > contentLen) {
//                System.out.println("出现粘包");
//                buffer.skipBytes(136);
//                int index = buffer.readerIndex();
//                int len = buffer.readableBytes();
//
//                ByteBuf slice = buffer.slice(index-4, len+4);
//                buffer.readerIndex(index + len);
//
//                out.add(slice.retain());
//                state = Step.NEW;
//                lastReadIndex = buffer.readerIndex();
//                contentLen = 0;
//                return;
//            } else {
//                return;
//            }
        }
    }
}
