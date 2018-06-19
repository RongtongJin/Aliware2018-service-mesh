#!/bin/bash

ETCD_HOST=etcd
ETCD_PORT=2379
ETCD_URL=http://$ETCD_HOST:$ETCD_PORT

echo ETCD_URL = $ETCD_URL

if [[ "$1" == "consumer" ]]; then
  echo "Starting consumer agent..."
  java -jar \
       -Xms1536M \
       -Xmx1536M \
       -Dtype=consumer \
       -Dserver.port=20000 \
       -Detcd.url=$ETCD_URL \
       -Dlevel=consumer \
       -Dlogs.dir=/root/logs \
       -Dio.netty.leakDetectionLevel=DISABLED \
       -Dio.netty.buffer.bytebuf.checkAccessible=false \
       /root/dists/mesh-agent.jar
elif [[ "$1" == "provider-small" ]]; then
  echo "Starting small provider agent..."
  java -jar \
       -Xms768M \
       -Xmx768M \
       -Dtype=provider \
       -Dserver.port=30000 \
       -Ddubbo.protocol.port=20880 \
       -Detcd.url=$ETCD_URL \
       -Dlogs.dir=/root/logs \
       -Dlevel=small \
       -Dio.netty.leakDetectionLevel=DISABLED \
       -Dio.netty.buffer.bytebuf.checkAccessible=false \
       /root/dists/mesh-agent.jar
elif [[ "$1" == "provider-medium" ]]; then
  echo "Starting medium provider agent..."
  java -jar \
       -Xms1536M \
       -Xmx1536M \
       -Dtype=provider \
       -Dserver.port=30000 \
       -Ddubbo.protocol.port=20880 \
       -Detcd.url=$ETCD_URL \
       -Dlogs.dir=/root/logs \
       -Dlevel=medium \
       -Dio.netty.leakDetectionLevel=DISABLED \
       -Dio.netty.buffer.bytebuf.checkAccessible=false \
       /root/dists/mesh-agent.jar
elif [[ "$1" == "provider-large" ]]; then
  echo "Starting large provider agent..."
  java -jar \
       -Xms2560M \
       -Xmx2560M \
       -Dtype=provider \
       -Dserver.port=30000 \
       -Ddubbo.protocol.port=20880 \
       -Detcd.url=$ETCD_URL \
       -Dlogs.dir=/root/logs \
       -Dlevel=large \
       -Dio.netty.leakDetectionLevel=DISABLED \
       -Dio.netty.buffer.bytebuf.checkAccessible=false \
       /root/dists/mesh-agent.jar
else
  echo "Unrecognized arguments, exit."
  exit 1
fi
