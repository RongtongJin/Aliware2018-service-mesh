package com.alibaba.dubbo.performance.demo.agent.consumeragent;


import io.netty.channel.Channel;

import java.util.concurrent.ConcurrentHashMap;

public class ChannelHolder {
    //传String还是传Integer效率高呢
    //key:DatagramPacket ID             value: TCP Channel
    private static ConcurrentHashMap<Integer,Channel> map=new ConcurrentHashMap<>();

    public static void put(Integer id,Channel ch) {map.put(id,ch);}

    public static Channel get(Integer id) {return map.get(id);}

    public static void remove(Integer id) {map.remove(id);}

}
