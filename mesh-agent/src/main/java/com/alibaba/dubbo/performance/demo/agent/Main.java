package com.alibaba.dubbo.performance.demo.agent;


import com.alibaba.dubbo.performance.demo.agent.consumeragent.tcp.TcpConsumerAgent;
import com.alibaba.dubbo.performance.demo.agent.consumeragent.udp.UdpConsumerAgent;
import com.alibaba.dubbo.performance.demo.agent.provideragent.tcp.TcpProviderAgent;
import com.alibaba.dubbo.performance.demo.agent.provideragent.udp.UdpProviderAgent;

public class Main {
    public static void main(String[] args) throws Exception{
        String type = System.getProperty("type");   // 获取type参数
        int port = Integer.valueOf(System.getProperty("server.port"));  //获取端口号
        if ("consumer".equals(type)){
            new UdpConsumerAgent().start(port);
        }else if("provider".equals(type)){
            new UdpProviderAgent().start(port);
        }else {
            System.err.println("Environment variable type is needed to set to provider or consumer.");
        }
    }
}
