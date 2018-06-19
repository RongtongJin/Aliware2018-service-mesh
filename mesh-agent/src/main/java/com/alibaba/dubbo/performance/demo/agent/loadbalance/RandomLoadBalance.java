package com.alibaba.dubbo.performance.demo.agent.loadbalance;

import com.alibaba.dubbo.performance.demo.agent.consumeragent.tcp.TcpChannel;
import com.alibaba.dubbo.performance.demo.agent.registry.Endpoint;
import com.alibaba.dubbo.performance.demo.agent.utils.ConstUtil;
import com.alibaba.dubbo.performance.demo.agent.utils.EnumKey;

import java.util.EnumMap;
import java.util.Map;

import io.netty.channel.EventLoopGroup;

public class RandomLoadBalance {
    public static EnumMap<EnumKey, Endpoint> endpoints = new EnumMap<EnumKey, Endpoint>(EnumKey.class);
    public static TcpChannel S = null;
    public static TcpChannel M = null;
    public static TcpChannel L = null;
    public static  int S_CNT=112;
    public static  int M_CNT=200;
    public static  int L_CNT=512-S_CNT-M_CNT;
    public static  int i=0;

    public static void init(EventLoopGroup workerGroup){
        for (Map.Entry<EnumKey, Endpoint> entry : endpoints.entrySet()) {
            try {
                if (entry.getKey().equals(EnumKey.S)) {
                    S = new TcpChannel(entry.getKey(),workerGroup, entry.getValue());
                    System.out.println("S--init");
                } else if (entry.getKey().equals(EnumKey.M)) {
                    M = new TcpChannel(entry.getKey(),workerGroup, entry.getValue());
                    System.out.println("M--init");
                } else {
                    L = new TcpChannel(entry.getKey(),workerGroup, entry.getValue());
                    System.out.println("L--init");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    public static void toStr(){
        System.out.println("S:"+S_CNT
                +"  M:"+M_CNT
                +"  L:"+L_CNT);
    }
    public static synchronized TcpChannel nextChanelFixed(){
        if (ConstUtil.IDEA_MODE) return S;
        while(true){
            ++i;
            switch (i%3){
                case 0:
                    if(L_CNT>0) {
                        L_CNT--;
                        return L;
                    }
                case 1:
                    if(M_CNT>0) {
                        M_CNT--;
                        return M;
                    }
                case 2:
                    if(S_CNT>0) {
                        S_CNT--;
                        return S;
                    }
            }
        }
    }

}
