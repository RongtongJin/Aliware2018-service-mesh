package com.alibaba.dubbo.performance.demo.agent.utils;

import io.netty.channel.FixedRecvByteBufAllocator;

public interface ConstUtil {
    /**
     * 服务器地址
     */
    byte CR = 13;
    int SKIP_HEAD=109;
    byte ZERO = '0';
    byte ONE = '1';
    byte EMP = ' ';
    byte[] template = "HTTP/1.1 200 OK\ncontent-length:  \n\n".getBytes(); //31
    int HEADER_LENGTH = 32;
    int MIN_LEN=256;
    boolean IDEA_MODE = System.getProperty("os.name").toLowerCase().indexOf("windows") >= 0 || System.getProperty("os.name").toLowerCase().indexOf("mac") >= 0;
    String DECODER="decoder";
    byte QUTO='"';
    byte[] QUTO2="\"}".getBytes();
    byte QUTO3='/';
    byte[] PATH_QUTO="{\"path\":\"".getBytes();

    byte SEM=';';
    FixedRecvByteBufAllocator LARGE_BUF_ALLOCATOR = new FixedRecvByteBufAllocator(1312);
    FixedRecvByteBufAllocator SMALL_BUF_ALLOCATOR = new FixedRecvByteBufAllocator(16);
    FixedRecvByteBufAllocator MEDIUM_BUF_ALLOCATOR = new FixedRecvByteBufAllocator(32);

}
