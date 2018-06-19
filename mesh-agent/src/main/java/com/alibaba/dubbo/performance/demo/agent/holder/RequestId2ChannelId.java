package com.alibaba.dubbo.performance.demo.agent.holder;

public final class RequestId2ChannelId {
    public static int[] requestId2ChannelId;

    static {
        String level=System.getProperty("level");
        if("small".equals(level)){
            requestId2ChannelId=new int[800000];
        }else{
            requestId2ChannelId=new int[1000000];
        }
    }

    public static final void put(int requestId,int channelId){requestId2ChannelId[requestId]=channelId;}

    public static final int get(int requestId){return requestId2ChannelId[requestId];}
}
