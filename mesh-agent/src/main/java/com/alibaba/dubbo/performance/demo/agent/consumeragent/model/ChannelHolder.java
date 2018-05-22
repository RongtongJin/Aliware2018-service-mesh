package com.alibaba.dubbo.performance.demo.agent.consumeragent.model;


import io.netty.channel.Channel;

import java.util.concurrent.ConcurrentHashMap;

public class ChannelHolder {
    //传String还是传Long效率高呢
    //key:DatagramPacket ID             value: TCP Channel
//    private static ConcurrentHashMap<Integer,Channel> map=new ConcurrentHashMap<>(1024);
    private static Channel[] channels=new Channel[700000];

//    public static void put(Integer id,Channel ch) {map.put(id,ch);}

    public static void put(int id,Channel ch) {channels[id]=ch;}

//    public static Channel get(Integer id) {return map.get(id);}

    public static Channel get(int id){return channels[id];}

//    public static void remove(Integer id) {map.remove(id);}

}
