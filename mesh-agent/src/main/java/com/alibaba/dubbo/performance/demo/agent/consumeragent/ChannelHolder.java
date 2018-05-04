package com.alibaba.dubbo.performance.demo.agent.consumeragent;


import io.netty.channel.Channel;

import java.util.concurrent.ConcurrentHashMap;

public class ChannelHolder {
    //传String还是传Integer效率高呢
    //key:DatagramPacket ID             value: TCP Channel
    private static ConcurrentHashMap<Long,Channel> map=new ConcurrentHashMap<>();

    public static void put(Long id,Channel ch) {map.put(id,ch);}

    public static Channel get(Long id) {return map.get(id);}

    public static void remove(Long id) {map.remove(id);}

}
