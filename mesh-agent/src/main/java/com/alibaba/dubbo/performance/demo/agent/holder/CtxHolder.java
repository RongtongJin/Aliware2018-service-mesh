package com.alibaba.dubbo.performance.demo.agent.holder;

import io.netty.channel.ChannelHandlerContext;


public final class CtxHolder {
    private static ChannelHandlerContext[] holder = new ChannelHandlerContext[1024];

    public static final void put(int id, ChannelHandlerContext ch) {
        holder[id]=ch;
    }

    public static final ChannelHandlerContext get(int id) {
        return holder[id];
    }

}
