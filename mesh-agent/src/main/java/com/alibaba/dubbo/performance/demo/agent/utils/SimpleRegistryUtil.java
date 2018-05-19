package com.alibaba.dubbo.performance.demo.agent.utils;

import com.alibaba.dubbo.performance.demo.agent.registry.Endpoint;
import com.alibaba.dubbo.performance.demo.agent.registry.IpHelper;
import com.coreos.jetcd.Client;
import com.coreos.jetcd.KV;
import com.coreos.jetcd.Lease;
import com.coreos.jetcd.data.ByteSequence;
import com.coreos.jetcd.kv.GetResponse;
import com.coreos.jetcd.options.GetOption;
import com.coreos.jetcd.options.PutOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public final class SimpleRegistryUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleRegistryUtil.class);

    // 向ETCD中注册服务
    public static void registerProvider(String registryAddress) throws Exception {
        LOGGER.info("etcd url:" + registryAddress);
        WeakReference<Client> client = new WeakReference<Client>(Client.builder().endpoints(registryAddress).build());
        Lease lease = client.get().getLeaseClient();
        KV kv = client.get().getKVClient();
        long leaseId = lease.grant(5).get().getID();
        Executors.newSingleThreadExecutor().submit(
                () -> {
                    try {
                        Lease.KeepAliveListener listener = lease.keepAlive(leaseId);
                        listener.listen();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
        );
        // 去etcd注册服务
        PutOption putOption = PutOption.newBuilder().withLeaseId(leaseId).build();
//        kv.put(ByteSequence.fromString("com.alibaba.dubbo.performance.demo.provider.IHelloService"),
//                ByteSequence.fromString(System.getProperty("server.port")), putOption
//        ).get();
        kv.put(ByteSequence.fromString("p" + System.getProperty("level")),
                ByteSequence.fromString( IpHelper.getHostIp() + ":" + System.getProperty("server.port")),
                putOption).get();
        LOGGER.info("注册地址:p" + System.getProperty("level") + "=" + IpHelper.getHostIp() + ":" + System.getProperty("server.port"));
    }

    public static List<Endpoint> findProviders(String registryAddress) throws Exception {
        WeakReference<Client> client = new WeakReference<Client>(Client.builder().endpoints(registryAddress).build());
        KV kvClient = client.get().getKVClient();
        List<Endpoint> endpoints = new ArrayList<>();
        ByteSequence key = ByteSequence.fromString("p");
        GetResponse response = kvClient.get(key, GetOption.newBuilder().withPrefix(key).build()).get();
        Endpoint endpoint = null;
        for (com.coreos.jetcd.data.KeyValue kv : response.getKvs()) {
            endpoint = new Endpoint(kv.getValue().toStringUtf8());
            LOGGER.info("获取endpoint:" + endpoint);
            endpoints.add(endpoint);
        }
        return endpoints;
        // GetResponse rpm = kv.get(ByteSequence.fromString("pm")).get();
        // GetResponse rpl = kv.get(ByteSequence.fromString("pl")).get();
        // System.err.println(rpm.getKvs().get(0).getValue().toStringUtf8());
        // System.err.println(rpl.getKvs().get(0).getValue().toStringUtf8());
    }
    public static List<Endpoint> findProviders(String registryAddress, List<Endpoint> endpoints) throws Exception {
        WeakReference<Client> client = new WeakReference<Client>(Client.builder().endpoints(registryAddress).build());
        KV kvClient = client.get().getKVClient();
        ByteSequence key = ByteSequence.fromString("p");
        GetResponse response = kvClient.get(key, GetOption.newBuilder().withPrefix(key).build()).get();
        Endpoint endpoint = null;
        for (com.coreos.jetcd.data.KeyValue kv : response.getKvs()) {
            endpoint = new Endpoint(kv.getValue().toStringUtf8());
            LOGGER.info("获取Provider Endpoint:" + endpoint);
            endpoints.add(endpoint);
        }
        return endpoints;
        // GetResponse rpm = kv.get(ByteSequence.fromString("pm")).get();
        // GetResponse rpl = kv.get(ByteSequence.fromString("pl")).get();
        // System.err.println(rpm.getKvs().get(0).getValue().toStringUtf8());
        // System.err.println(rpl.getKvs().get(0).getValue().toStringUtf8());
    }
    public static void main(String[] args) {
        String regist = "http://127.0.0.1:2379";
        try {
            registerProvider(regist);
            findProviders(regist);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
