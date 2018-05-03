package com.alibaba.dubbo.performance.demo.agent.ConsumerAgentTest;


import static io.netty.handler.codec.http.HttpHeaders.Names.CONNECTION;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

import com.alibaba.dubbo.performance.demo.agent.ComsumerAgent.HttpServer;
import com.alibaba.dubbo.performance.demo.agent.ComsumerAgent.HttpServerInboundHandler;
import com.alibaba.dubbo.performance.demo.agent.ComsumerAgent.RemoteInitializer;
import com.alibaba.dubbo.performance.demo.agent.registry.Endpoint;
import com.alibaba.dubbo.performance.demo.agent.registry.IRegistry;
import com.alibaba.fastjson.JSON;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;
import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.io.IOException;
import java.net.URI;
import java.util.List;


public class HttpServerInboundHandlerTest extends SimpleChannelInboundHandler<FullHttpRequest> {

    private static Log log = LogFactory.getLog(HttpServerInboundHandlerTest.class);

    private List<ProviderAgentChannel> channelList=null;

    HttpServerInboundHandlerTest(List<ProviderAgentChannel> channelList){
        this.channelList=channelList;
    }

    private HttpRequest request;

    @Override
    public void channelRead0(ChannelHandlerContext ctx, FullHttpRequest msg)
            throws Exception {

        //fix me:会不会有后续的httpcontent没有接受到或者重发
        ByteBuf buf = msg.content();
        System.out.println(buf.toString(io.netty.util.CharsetUtil.UTF_8));

        /*

        //fix me:又要重启一个线程
        //EventLoopGroup workerGroup = new NioEventLoopGroup();
        //创建一个 Bootstrap 类的实例以连接到远程主机
        Bootstrap b = new Bootstrap()
                .channel(NioSocketChannel.class)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .handler(new RemoteInitializerTest(ctx.channel()))
                //使用与分配给已被接受的子Channel相同的EventLoopGroup,这样可以吗。。
                .group(ctx.channel().eventLoop().parent());
        //fix me:能不能换成异步
        System.out.println("HttpContent2");
        ChannelFuture f=b.connect("127.0.0.1",8844).sync();
        f.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                if(!channelFuture.isSuccess()){
                    System.err.println("connect failed!");
                    channelFuture.cause().printStackTrace();
                }
            }
        });*/
        System.out.println("HttpContent2");
        Channel channel=channelList.get(0).getChannel();
        if(channel==null){
            System.out.println("channel==null");
        }
        System.out.println("HttpContent3");
        URI uri = new URI("http://127.0.0.1:8844");

        String msg2 = "Are you ok?";
        DefaultFullHttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET,
                uri.toASCIIString() , Unpooled.wrappedBuffer(msg2.getBytes("UTF-8")));

        // 构建http请求
       // request.headers().set(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
        //request.headers().set(HttpHeaders.Names.CONTENT_LENGTH, request.content().readableBytes());
        // 发送http请求
        channel.writeAndFlush(request).addListener(x->{
            if(!x.isSuccess()){
                System.err.println("write failed!");
                x.cause().printStackTrace();
            }
        });

    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error(cause.getMessage());
        ctx.close();
    }

}