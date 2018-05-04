package com.alibaba.dubbo.performance.demo.agent.ConsumerAgentTest;

        import com.alibaba.dubbo.performance.demo.agent.dubbo.model.Bytes;
        import com.alibaba.fastjson.JSON;
        import io.netty.bootstrap.Bootstrap;
        import io.netty.buffer.ByteBuf;
        import io.netty.buffer.Unpooled;
        import io.netty.channel.ChannelHandlerContext;
        import io.netty.channel.EventLoopGroup;
        import io.netty.channel.SimpleChannelInboundHandler;
        import io.netty.channel.nio.NioEventLoopGroup;
        import io.netty.channel.socket.DatagramPacket;
        import io.netty.channel.socket.nio.NioDatagramChannel;

        import java.net.InetSocketAddress;


public class UDPServerTest2 {

    public void start(int port) throws Exception{
        EventLoopGroup eventLoopGroup=new NioEventLoopGroup();
        try {
            Bootstrap bootstrap=new Bootstrap()
                    .group(eventLoopGroup)
                    .channel(NioDatagramChannel.class)
                    //.option(ChannelOption.SO_BACKLOG, 128)    //设置缓存队列
                    //.option(ChannelOption.SO_RCVBUF, 1024 * 1024)// 设置UDP读缓冲区为1M
                    //.option(ChannelOption.SO_SNDBUF, 1024 * 1024)// 设置UDP写缓冲区为1M
                    .handler(new SimpleChannelInboundHandler<DatagramPacket>() {
                        @Override
                        public void channelRead0(ChannelHandlerContext ctx,
                                                 DatagramPacket msg) throws Exception {
                            ByteBuf buf=msg.content();
                            buf.retain();
                            // ByteBuf bufId=buf.slice(0,4);
                            // ByteBuf bufStr=buf.slice(4,buf.readableBytes());
                            // System.out.println("id="+Bytes.bytes2int(bufId.array()));
                            //String s=new String(bufStr.array());
                            String s=new String(Unpooled.copiedBuffer(buf).array());
                            System.out.println("str="+s);
                            DatagramPacket dp=new DatagramPacket(buf,msg.sender());
                            ctx.channel().writeAndFlush(dp).sync();
//                                    .addListener(cf->{
//                                System.err.println("error in udpserver write msg.");
//                                cf.cause().printStackTrace();
//                            });
                            //JSON.parse(s.hashCode());
                        }
                    });
            bootstrap.bind(new InetSocketAddress(port)).sync().channel().closeFuture().await();

//                    .addListener(cf->{
//                if (!cf.isSuccess()){
//                    System.err.println("error in udpserver channel bound.");
//                    cf.cause().printStackTrace();
//                }else{
//                    System.out.println("success!");
//                }
//            });
        }finally {
            eventLoopGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws Exception{
        new UDPServerTest2().start(8855);
    }
}
