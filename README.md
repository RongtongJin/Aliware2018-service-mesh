intellij idea下跑程序，在Consumer中指定虚拟机参数

-Ddubbo.protocol.port=20889 -Ddubbo.application.qos.enable=false



另外与etcd相关的请注释
-Dtype=consumer
-Dserver.port=20000
-Detcd.url=http://127.0.0.1:2379


-Dtype=provider
-Dserver.port=30000
-Ddubbo.protocol.port=20889
-Detcd.url=http://127.0.0.1:2379

-Dtype=provider
-Dserver.port=30000
-Ddubbo.protocol.port=20889
-Detcd.url=http://127.0.0.1:2379
-Ddubbo.application.qos.enable=false