package com.alibaba.dubbo.performance.demo.agent.utils;

public interface ConstUtil {
    /**
     * 服务器地址
     */
    String SERVER = "127.0.0.1";

    int PORT = 20000;
    byte CR = 13;
    byte LF = 10;
    byte SEP = '\n';
    byte N = '-';
    byte ZERO = '0';
    byte ONE = '1';
    byte two = '2';
    byte l = 'l';
    byte e = 'e';
    byte n = 'n';
    byte EMP = ' ';
    int REQUEST_SIZE=1024*2;
    byte[] template="HTTP/1.1 200 OK\ncontent-length:".getBytes(); //31

}
