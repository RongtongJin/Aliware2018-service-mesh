package com.alibaba.dubbo.performance.demo.agent.ComsumerAgent;

import static io.netty.handler.codec.http.HttpHeaders.Names.CONNECTION;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

import com.alibaba.dubbo.performance.demo.agent.registry.Endpoint;
import com.alibaba.dubbo.performance.demo.agent.registry.IRegistry;
import com.alibaba.fastjson.JSON;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.channel.*;
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
import java.util.concurrent.atomic.AtomicInteger;

public class HttpServerInboundHandler extends ChannelInboundHandlerAdapter {

    private static Log log = LogFactory.getLog(HttpServerInboundHandler.class);

    private List<Endpoint> endpoints = null;

    public static AtomicInteger count=new AtomicInteger(0);


    private HttpRequest request;

    private Endpoint endpoint = null;

    HttpServerInboundHandler(List<Endpoint> endpoints){
        this.endpoints=endpoints;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg)
            throws Exception {
        if (msg instanceof HttpRequest) {
            //fix me 有用吗？
            request = (HttpRequest) msg;
            String uri = request.uri();
            System.out.println("Uri:" + uri);
        }
        //fix me:会不会有后续的httpcontent没有接受到或者重发
        if (msg instanceof HttpContent) {
            HttpContent content = (HttpContent) msg;
            ByteBuf buf = content.content();

            //按照性能简单负载均衡
            int x=count.incrementAndGet()%6;
            if(x==0)
                endpoint=endpoints.get(0);
            else if(1<=x&&x<=2)
                endpoint=endpoints.get(1);
            else
                endpoint=endpoints.get(2);

            URI uri = new URI("http://" + endpoint.getHost() + ":" + endpoint.getPort());
            //创建一个 Bootstrap 类的实例以连接到远程主机
            Bootstrap b = new Bootstrap()
                    //使用与分配给已被接受的子Channel相同的EventLoop
                    .group(ctx.channel().eventLoop())
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .option(ChannelOption.ALLOCATOR, UnpooledByteBufAllocator.DEFAULT)
                    .channel(NioSocketChannel.class)
                    .handler(new RemoteInitializer(ctx));
            //fix me:能不能换成异步
            ChannelFuture f=b.connect(endpoint.getHost(),endpoint.getPort());
            DefaultFullHttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST,
                   uri.toASCIIString() , Unpooled.copiedBuffer(buf));
            buf.release();

            f.channel().writeAndFlush(request);

            f.channel().closeFuture();
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error(cause.getMessage());
        System.out.println(cause.getMessage());
        ctx.close();
    }

}