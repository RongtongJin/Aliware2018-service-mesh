package com.alibaba.dubbo.performance.demo.agent.utils;

import com.alibaba.dubbo.performance.demo.agent.protocal.AgentHttpRequest;
import com.alibaba.dubbo.performance.demo.agent.protocal.MyHttpRequestDecoder;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public final class HttpDecodeUtil {
    public static final AttributeKey<AgentHttpRequest> NETTY_CHANNEL_KEY = AttributeKey.valueOf("1");
    private static Logger logger = LoggerFactory.getLogger(MyHttpRequestDecoder.class);

    public static final void decode(ChannelHandlerContext ctx, ByteBuf buffer, List<Object> out) throws Exception {
        while(buffer.readByte()!=ConstUtil.l){
        }

        if (buffer.readByte() == ConstUtil.l && buffer.readByte() == ConstUtil.e && buffer.readByte() == ConstUtil.n) {
            System.err.println("L");
            buffer.readerIndex(buffer.readerIndex() + 4);
        } else {
            return;
        }
        int contentLen = 0;
        byte b = buffer.readByte();
        while (b != ConstUtil.CR) {
            if (b != ConstUtil.EMP) {
                contentLen = contentLen * 10 + (b - ConstUtil.ZERO);
            }
            b = buffer.readByte();
        }

        if (buffer.readByte() == ConstUtil.LF) {
            //step=Step.body;
            buffer.readerIndex(buffer.readerIndex() + 103);
            if (buffer.readByte() == ConstUtil.CR
                    && buffer.readByte() == ConstUtil.LF
                    && buffer.readByte() == ConstUtil.CR
                    && buffer.readByte() == ConstUtil.LF) {

                if (buffer.readableBytes() == contentLen) {
                    //buffer.position(buffer.limit());
//					AgentHttpRequest agentHttpRequest=hashMap.get(channelContext.hashCode());
//					if(agentHttpRequest==null){
//						agentHttpRequest=new AgentHttpRequest(buffer,channelContext);
//						hashMap.put(channelContext.hashCode(),agentHttpRequest);
//					}else{
//						agentHttpRequest.setBody(buffer,channelContext);
//					}
                    // AgentHttpRequest agentHttpRequest=new AgentHttpRequest();
//                    AgentHttpRequest agentHttpRequest = ctx.channel().attr(NETTY_CHANNEL_KEY).get();
//                    if (agentHttpRequest == null) {
//                        agentHttpRequest = new AgentHttpRequest(ctx.alloc().buffer(ConstUtil.REQUEST_SIZE));
//                        ctx.channel().attr(NETTY_CHANNEL_KEY).set(agentHttpRequest);
//                    }
//                    AgentHttpRequest   agentHttpRequest = new AgentHttpRequest(ctx.alloc().buffer(ConstUtil.REQUEST_SIZE));
//                    agentHttpRequest.setBody(buffer, ctx);
//                    //System.err.println("成功解码："+buffer);
//                    out.add(agentHttpRequest);
                    return;
                } else {
                    logger.info("失败" + buffer.readableBytes() + ":" + contentLen);
                    return;
                }
            } else {
                logger.error("跨越到body失败！");
            }
        } else {
            logger.error("解析长度失败！");
        }
    }
}
