intellij idea下跑程序，在Consumer中指定虚拟机参数

-Ddubbo.protocol.port=20889 -Ddubbo.application.qos.enable=false



另外与etcd相关的请注释
http://39.104.105.27:55555

githu代码 
git@code.aliyun.com:jinrongtong5/aliware-service-mesh.git
镜像地址:
https://cr.console.aliyun.com/?accounttraceid=87511ad1-c246-4283-a400-ca1647cd81b2#/imageList

# cosumer 
-Xms1536M -Xmx1536M -Dtype=consumer -Dserver.port=20000 -Detcd.url=http://127.0.0.1:2379 -Dlogs.dir=C://logs -Dio.netty.leakDetectionLevel=PARANOID

# provider
-Xms512M -Xmx512M -Dtype=provider -Dserver.port=30000 -Ddubbo.protocol.port=20880 -Detcd.url=http://127.0.0.1:2379 -Dlogs.dir=C://logs -Dlevel=small -Dio.netty.leakDetectionLevel=PARANOID
# 打印GC
-XX:+PrintGCDetails 