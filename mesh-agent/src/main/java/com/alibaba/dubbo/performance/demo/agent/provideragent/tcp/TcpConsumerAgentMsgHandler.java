package com.alibaba.dubbo.performance.demo.agent.provideragent.tcp;


import com.alibaba.dubbo.performance.demo.agent.holder.RequestId2ChannelId;
import com.alibaba.dubbo.performance.demo.agent.holder.bytebuf.FixedLengthCompositeByteBuf;
import com.alibaba.dubbo.performance.demo.agent.utils.Bytes;
import com.alibaba.dubbo.performance.demo.agent.utils.ConstUtil;
import com.alibaba.dubbo.performance.demo.agent.utils.JsonUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.EmptyByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollChannelOption;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.util.CharsetUtil;


public class TcpConsumerAgentMsgHandler extends ChannelInboundHandlerAdapter {

    // header length.
    protected static final int HEADER_LENGTH = 16;
    // magic header.
    protected static final short MAGIC = (short) 0xdabb;
    // message flag.
    protected static final byte FLAG_REQUEST = (byte) 0x80;
    protected static final byte FLAG_TWOWAY = (byte) 0x40;

    private ByteBuf headerBuf=null;

    private ByteBuf frontBody=null;

    private ByteBuf quto=null;

    private ByteBuf quto2=null;

    private ByteBuf quto3=null;

    private ByteBuf quto4=null;

    private ByteBuf qutos=null;

    private static int requestId=0;

    private static int len;

    private ChannelHandlerContext outCtx=null;

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        headerBuf= ctx.alloc().directBuffer(HEADER_LENGTH);
        frontBody=ctx.alloc().directBuffer();
        quto=ctx.alloc().directBuffer();
        quto2=ctx.alloc().directBuffer();
        quto3=ctx.alloc().directBuffer();
        quto4=ctx.alloc().directBuffer();
        qutos=ctx.alloc().directBuffer();

        quto.writeByte(ConstUtil.QUTO);
        quto.writeBytes(System.lineSeparator().getBytes());
        quto.writeByte(ConstUtil.QUTO);

        quto2.writeByte(ConstUtil.SEM);
        quto2.writeByte(ConstUtil.QUTO);
        quto2.writeBytes(System.lineSeparator().getBytes());
        quto2.writeByte(ConstUtil.QUTO);

        quto3.writeByte(ConstUtil.QUTO);
        quto3.writeBytes(System.lineSeparator().getBytes());
        quto3.writeBytes(ConstUtil.PATH_QUTO);

        quto4.writeBytes(ConstUtil.QUTO2);
        quto4.writeBytes(System.lineSeparator().getBytes());

        qutos.writeByte(ConstUtil.QUTO3);

        byte[] header = new byte[HEADER_LENGTH];
        //header
        Bytes.short2bytes(MAGIC, header);
        // set request and serialization flag.
        header[2] = (byte) (FLAG_REQUEST | 6);
        header[2] |= FLAG_TWOWAY;

        headerBuf.writeBytes(header);

        //front body
        ByteArrayOutputStream frontOut = new ByteArrayOutputStream();
        PrintWriter frontWriter = new PrintWriter(new OutputStreamWriter(frontOut));

        try{
            //Dubbo version
            JsonUtils.writeObject(null,frontWriter);
            //Service name
            JsonUtils.writeObject(null,frontWriter);
            //Service version
            JsonUtils.writeObject(null,frontWriter);
        }catch (IOException e){
            e.printStackTrace();
        }

        frontBody.writeBytes(frontOut.toByteArray());
        frontBody.writeByte('"');

        len=frontBody.readableBytes()+quto.readableBytes()+quto2.readableBytes()+quto3.readableBytes()+quto4.readableBytes();

        System.out.print(frontBody.toString(CharsetUtil.UTF_8));
        System.out.print(quto.toString(CharsetUtil.UTF_8));
        System.out.print(quto2.toString(CharsetUtil.UTF_8));
        System.out.print(quto4.toString(CharsetUtil.UTF_8));
        System.out.println(len);
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        headerBuf.release();
        frontBody.release();
        quto.release();
        quto2.release();
        quto3.release();
        quto4.release();
        qutos.release();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        TcpRpcMsgHandler.setOutCtx(ctx.channel());
        Class<? extends SocketChannel> channelClass= Epoll.isAvailable() ? EpollSocketChannel.class:NioSocketChannel.class;
        ChannelFuture cf = new Bootstrap()
                .group(ctx.channel().eventLoop())
                .channel(channelClass)
                .option(ChannelOption.SO_KEEPALIVE, Boolean.TRUE)
                .option(ChannelOption.TCP_NODELAY, Boolean.TRUE)
                .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .option(EpollChannelOption.TCP_QUICKACK,Boolean.TRUE)
                .option(ChannelOption.RCVBUF_ALLOCATOR, ConstUtil.MEDIUM_BUF_ALLOCATOR)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        ChannelPipeline pipeline = socketChannel.pipeline();
                        pipeline.addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE,12,4,0,0));
                        pipeline.addLast(new TcpRpcMsgHandler());
                    }
                })
                .connect("127.0.0.1", 20880);
        outCtx=cf.channel().pipeline().firstContext();
        System.out.println("connect server...");
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf byteBuf = (ByteBuf) msg;
        ByteBuf serviceName=byteBuf.slice(12,57);
        ByteBuf methodName=byteBuf.slice(77,4);
        ByteBuf paraType1=byteBuf.slice(103,5);
        ByteBuf paraType2=byteBuf.slice(111,4);
        ByteBuf paraType3=byteBuf.slice(118,6);
        short ChannelId=byteBuf.readShort();
        RequestId2ChannelId.put(requestId,ChannelId);
        byteBuf.skipBytes(136);
        headerBuf.setInt(8,requestId++);
        headerBuf.setInt(12,byteBuf.readableBytes()+len+78);
        CompositeByteBuf sendBuf=PooledByteBufAllocator.DEFAULT.compositeDirectBuffer(14);
        sendBuf.addComponents(true,headerBuf.retain(),frontBody.retain(),methodName.retain(),quto.retain(),paraType1.retain(),qutos.retain(),paraType2.retain(),qutos.retain(),paraType3.retain(),quto2.retain(),byteBuf.retain(),quto3.retain(),serviceName,quto4.retain());
        outCtx.writeAndFlush(sendBuf,outCtx.voidPromise());
    }
}
