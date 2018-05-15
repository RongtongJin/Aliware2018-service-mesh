package com.alibaba.dubbo.performance.demo.agent.registry;

import com.coreos.jetcd.Client;
import com.coreos.jetcd.KV;
import com.coreos.jetcd.Lease;
import com.coreos.jetcd.data.ByteSequence;
import com.coreos.jetcd.kv.GetResponse;
import com.coreos.jetcd.options.GetOption;
import com.coreos.jetcd.options.PutOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

public class EtcdRegistry implements IRegistry{
    private Logger logger = LoggerFactory.getLogger(EtcdRegistry.class);
    // 该EtcdRegistry没有使用etcd的Watch机制来监听etcd的事件
    // 添加watch，在本地内存缓存地址列表，可减少网络调用的次数
    // 使用的是简单的随机负载均衡，如果provider性能不一致，随机策略会影响性能

    private Lease lease;
    private KV kv;
    private long leaseId;

    public EtcdRegistry(String registryAddress) {
        System.out.println(registryAddress);
        Client client = Client.builder().endpoints(registryAddress).build();
        this.lease   = client.getLeaseClient();
        this.kv      = client.getKVClient();
        try {
            this.leaseId = lease.grant(30).get().getID();
        } catch (Exception e) {
            e.printStackTrace();
        }
        //这部分能不能优化，获取一次后不在需要连接
        keepAlive();

        String type = System.getProperty("type");   // 获取type参数
        if ("provider".equals(type)){
            // 如果是provider，去etcd注册服务
            try {
                int port = Integer.valueOf(System.getProperty("server.port"));
                register("com.alibaba.dubbo.performance.demo.provider.IHelloService",port);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // 向ETCD中注册服务
    public void register(String serviceName,int port) throws Exception {
        // 服务注册的key为-->/serviceName/level    /com.some.package.IHelloService/small
        String strKey = MessageFormat.format("/{0}/{1}",serviceName,System.getProperty("level"));
        ByteSequence key = ByteSequence.fromString(strKey);
        // 服务注册的val为-->ip:port   /127.0.0.1:30000
        String strVal= MessageFormat.format("{0}:{1}",IpHelper.getHostIp(),String.valueOf(port));
        ByteSequence val = ByteSequence.fromString(strVal);
        kv.put(key,val, PutOption.newBuilder().withLeaseId(leaseId).build()).get();
        logger.info("Register a new service at:" + strKey);
    }

    // 发送心跳到ETCD,表明该host是活着的
    public void keepAlive(){
        Executors.newSingleThreadExecutor().submit(
                () -> {
                    try {
                        Lease.KeepAliveListener listener = lease.keepAlive(leaseId);
                        listener.listen();
                        logger.info("KeepAlive lease:" + leaseId + "; Hex format:" + Long.toHexString(leaseId));
                    } catch (Exception e) { e.printStackTrace(); }
                }
        );
    }

    public Map<String,Endpoint> find(String serviceName) throws Exception {

        String strKey = MessageFormat.format("/{0}",serviceName);
        ByteSequence key  = ByteSequence.fromString(strKey);
        GetResponse response = kv.get(key, GetOption.newBuilder().withPrefix(key).build()).get();

        //List<Endpoint> endpoints = new ArrayList<>();

        Map<String,Endpoint> level2EndPoint=new HashMap<>();

        for (com.coreos.jetcd.data.KeyValue kv : response.getKvs()){
            String k = kv.getKey().toStringUtf8();
            int index = k.lastIndexOf("/");
            String levelStr = k.substring(index + 1,k.length());

            String v= kv.getValue().toStringUtf8();
            String host = v.split(":")[0];
            int port = Integer.valueOf(v.split(":")[1]);

            level2EndPoint.put(levelStr,new Endpoint(host,port));
        }
        return level2EndPoint;
    }
}
