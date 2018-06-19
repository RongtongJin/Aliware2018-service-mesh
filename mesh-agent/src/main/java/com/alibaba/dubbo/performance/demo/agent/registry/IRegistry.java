package com.alibaba.dubbo.performance.demo.agent.registry;

import com.alibaba.dubbo.performance.demo.agent.utils.EnumKey;

import java.util.Map;

public interface IRegistry {

    // 注册服务
    void register(String serviceName, int port) throws Exception;

    Map<EnumKey,Endpoint> find(String serviceName) throws Exception;
}
