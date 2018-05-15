package com.alibaba.dubbo.performance.demo.agent.registry;

import java.util.List;
import java.util.Map;

public interface IRegistry {

    // 注册服务
    void register(String serviceName, int port) throws Exception;

    Map<String,Endpoint> find(String serviceName) throws Exception;
}
