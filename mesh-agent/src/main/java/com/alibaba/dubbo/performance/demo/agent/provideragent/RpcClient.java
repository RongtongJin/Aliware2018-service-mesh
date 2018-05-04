package com.alibaba.dubbo.performance.demo.agent.provideragent;

import com.alibaba.dubbo.performance.demo.agent.utils.JsonUtils;
import com.alibaba.dubbo.performance.demo.agent.provideragent.rpcmodel.Request;
import com.alibaba.dubbo.performance.demo.agent.provideragent.rpcmodel.RpcInvocation;


import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

public class RpcClient {

    public static void invoke(long id, String parameter) throws Exception {

        RpcInvocation invocation = new RpcInvocation();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(out));
        JsonUtils.writeObject(parameter, writer);
        invocation.setArguments(out.toByteArray());

        Request request = new Request();
        request.setId(id);
        request.setVersion("2.0.0");
        request.setTwoWay(true);
        request.setData(invocation);
        ProviderChannelManager.getChannel().writeAndFlush(request);
    }
}
