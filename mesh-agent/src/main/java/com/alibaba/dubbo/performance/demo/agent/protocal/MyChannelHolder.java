package com.alibaba.dubbo.performance.demo.agent.protocal;

import com.koloboke.collect.map.IntObjMap;
import com.koloboke.collect.map.hash.HashIntObjMaps;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.SocketChannelConfig;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

public final class MyChannelHolder {

    private static IntObjMap<Channel> map = HashIntObjMaps.newUpdatableMap(256*2);

    public static void put(int id, Channel ch) {
        map.put(id, ch);
    }

    public static Channel get(int id) {
        return map.get(id);
    }

    public static void remove(int id) {
        map.remove(id);
    }

    public static void main(String[] args) {
        map.put(1, new SocketChannel() {
            @Override
            public ServerSocketChannel parent() {
                return null;
            }

            @Override
            public SocketChannelConfig config() {
                return null;
            }

            @Override
            public InetSocketAddress localAddress() {
                return null;
            }

            @Override
            public InetSocketAddress remoteAddress() {
                return null;
            }

            @Override
            public boolean isInputShutdown() {
                return false;
            }

            @Override
            public ChannelFuture shutdownInput() {
                return null;
            }

            @Override
            public ChannelFuture shutdownInput(ChannelPromise promise) {
                return null;
            }

            @Override
            public boolean isOutputShutdown() {
                return false;
            }

            @Override
            public ChannelFuture shutdownOutput() {
                return null;
            }

            @Override
            public ChannelFuture shutdownOutput(ChannelPromise promise) {
                return null;
            }

            @Override
            public boolean isShutdown() {
                return false;
            }

            @Override
            public ChannelFuture shutdown() {
                return null;
            }

            @Override
            public ChannelFuture shutdown(ChannelPromise promise) {
                return null;
            }

            @Override
            public ChannelId id() {
                return null;
            }

            @Override
            public EventLoop eventLoop() {
                return null;
            }

            @Override
            public boolean isOpen() {
                return false;
            }

            @Override
            public boolean isRegistered() {
                return false;
            }

            @Override
            public boolean isActive() {
                return false;
            }

            @Override
            public ChannelMetadata metadata() {
                return null;
            }

            @Override
            public ChannelFuture closeFuture() {
                return null;
            }

            @Override
            public boolean isWritable() {
                return false;
            }

            @Override
            public long bytesBeforeUnwritable() {
                return 0;
            }

            @Override
            public long bytesBeforeWritable() {
                return 0;
            }

            @Override
            public Unsafe unsafe() {
                return null;
            }

            @Override
            public ChannelPipeline pipeline() {
                return null;
            }

            @Override
            public ByteBufAllocator alloc() {
                return null;
            }

            @Override
            public Channel read() {
                return null;
            }

            @Override
            public Channel flush() {
                return null;
            }

            @Override
            public ChannelFuture bind(SocketAddress localAddress) {
                return null;
            }

            @Override
            public ChannelFuture connect(SocketAddress remoteAddress) {
                return null;
            }

            @Override
            public ChannelFuture connect(SocketAddress remoteAddress, SocketAddress localAddress) {
                return null;
            }

            @Override
            public ChannelFuture disconnect() {
                return null;
            }

            @Override
            public ChannelFuture close() {
                return null;
            }

            @Override
            public ChannelFuture deregister() {
                return null;
            }

            @Override
            public ChannelFuture bind(SocketAddress localAddress, ChannelPromise promise) {
                return null;
            }

            @Override
            public ChannelFuture connect(SocketAddress remoteAddress, ChannelPromise promise) {
                return null;
            }

            @Override
            public ChannelFuture connect(SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise) {
                return null;
            }

            @Override
            public ChannelFuture disconnect(ChannelPromise promise) {
                return null;
            }

            @Override
            public ChannelFuture close(ChannelPromise promise) {
                return null;
            }

            @Override
            public ChannelFuture deregister(ChannelPromise promise) {
                return null;
            }

            @Override
            public ChannelFuture write(Object msg) {
                return null;
            }

            @Override
            public ChannelFuture write(Object msg, ChannelPromise promise) {
                return null;
            }

            @Override
            public ChannelFuture writeAndFlush(Object msg, ChannelPromise promise) {
                return null;
            }

            @Override
            public ChannelFuture writeAndFlush(Object msg) {
                return null;
            }

            @Override
            public ChannelPromise newPromise() {
                return null;
            }

            @Override
            public ChannelProgressivePromise newProgressivePromise() {
                return null;
            }

            @Override
            public ChannelFuture newSucceededFuture() {
                return null;
            }

            @Override
            public ChannelFuture newFailedFuture(Throwable cause) {
                return null;
            }

            @Override
            public ChannelPromise voidPromise() {
                return null;
            }

            @Override
            public <T> Attribute<T> attr(AttributeKey<T> key) {
                return null;
            }

            @Override
            public <T> boolean hasAttr(AttributeKey<T> key) {
                return false;
            }

            @Override
            public int compareTo(Channel o) {
                return 0;
            }
        });    }

}
