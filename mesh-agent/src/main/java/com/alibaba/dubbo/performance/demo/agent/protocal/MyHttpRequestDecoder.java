package com.alibaba.dubbo.performance.demo.agent.protocal;

import com.alibaba.dubbo.performance.demo.agent.consumeragent.ChannelHolder;
import com.alibaba.dubbo.performance.demo.agent.utils.ConstUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class MyHttpRequestDecoder extends ByteToMessageDecoder {
    public static final AttributeKey<AgentHttpRequest> NETTY_CHANNEL_KEY = AttributeKey.valueOf("1");
    private static Logger logger = LoggerFactory.getLogger(MyHttpRequestDecoder.class);
    private static AtomicLong genId=new AtomicLong();
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf buffer, List<Object> out) throws Exception {
        buffer.readerIndex(25);
        //下面可以写死
        if (buffer.readByte() == ConstUtil.l && buffer.readByte() == ConstUtil.e && buffer.readByte() == ConstUtil.n) {
            // buffer.readerIndex(buffer.readerIndex() + 4);
            buffer.skipBytes(4);
        } else {
            return;
        }
        //上面可以写死
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
            buffer.readerIndex(buffer.readerIndex() + 104);
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
                    //buffer.indexOf(buffer.readerIndex(),buffer.r)
                    buffer.skipBytes(136);

                    AgentHttpRequest   agentHttpRequest = new AgentHttpRequest(ctx.alloc().ioBuffer(ConstUtil.REQUEST_SIZE));
                    long id=genId.getAndIncrement();
                    agentHttpRequest.setStrBody(id,buffer);
                    ChannelHolder.put(id,ctx.channel());
                   // logger.info(agentHttpRequest.bodyBuf.toString());
                    out.add(agentHttpRequest);
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
    protected void decode1(ChannelHandlerContext ctx, ByteBuf buffer, List<Object> out) throws Exception{
        //System.err.println("decode:"+ctx.channel().hashCode());
        //logger.info(buffer.toString(Charset.forName("utf8")));
        buffer.readerIndex(25);
        //下面可以写死
        if (buffer.readByte() == ConstUtil.l && buffer.readByte() == ConstUtil.e && buffer.readByte() == ConstUtil.n) {
            // buffer.readerIndex(buffer.readerIndex() + 4);
            buffer.skipBytes(4);
        } else {
            return;
        }
        //上面可以写死
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
            buffer.readerIndex(buffer.readerIndex() + 104);
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
                    AgentHttpRequest   agentHttpRequest = new AgentHttpRequest(ctx.alloc().buffer(ConstUtil.REQUEST_SIZE));
                    agentHttpRequest.setBody(buffer, ctx.channel());

                    out.add(agentHttpRequest);
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
