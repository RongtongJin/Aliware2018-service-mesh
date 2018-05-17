package com.alibaba.dubbo.performance.demo.agent.protocal;

import com.alibaba.dubbo.performance.demo.agent.utils.ConstUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class MyHttpRequestDecoder extends ByteToMessageDecoder {
    private static Logger logger= LoggerFactory.getLogger(MyHttpRequestDecoder.class);
    public static final AttributeKey<AgentHttpRequest> NETTY_CHANNEL_KEY = AttributeKey.valueOf("1");
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf buffer, List<Object> out) throws Exception {
        AgentHttpRequest agentHttpRequest=ctx.channel().attr(NETTY_CHANNEL_KEY).get();
        if(agentHttpRequest==null){
            agentHttpRequest=new AgentHttpRequest();
            ctx.channel().attr(NETTY_CHANNEL_KEY).set(agentHttpRequest);
        }

        buffer.readerIndex(74);
        if(buffer.readByte()=='L'&&buffer.readByte()=='e'&&buffer.readByte()=='n'){
            buffer.readerIndex(buffer.readerIndex()+4);
        }else{
            return ;
        }
        int contentLen=0;
        byte b=buffer.readByte();
        while(b!= ConstUtils.CR){
            if(b!=' '){
                contentLen=contentLen*10+(b-'0');
            }
            b=buffer.readByte();

        }

        if(buffer.readByte()== ConstUtils.LF){
            //step=Step.body;
            buffer.readerIndex(buffer.readerIndex()+95);
            if(buffer.readByte()== ConstUtils.CR
                    &&buffer.readByte()== ConstUtils.LF
                    &&buffer.readByte()== ConstUtils.CR
                    &&buffer.readByte()== ConstUtils.LF){

                if(buffer.readableBytes()==contentLen){
                    //buffer.position(buffer.limit());
//					AgentHttpRequest agentHttpRequest=hashMap.get(channelContext.hashCode());
//					if(agentHttpRequest==null){
//						agentHttpRequest=new AgentHttpRequest(buffer,channelContext);
//						hashMap.put(channelContext.hashCode(),agentHttpRequest);
//					}else{
//						agentHttpRequest.setBody(buffer,channelContext);
//					}
                   // AgentHttpRequest agentHttpRequest=new AgentHttpRequest();
                    agentHttpRequest.setBody(buffer,ctx);
                    //System.err.println("成功解码："+buffer);

                    out.add(agentHttpRequest);
                    return ;
                }else{
                    logger.info("失败"+buffer.readableBytes()+":"+contentLen);
                    return ;
                }
            }else{
                logger.info("跨越到body失败！");
            }
        }else{
            logger.info("解析长度失败！");
        }
    }
}
