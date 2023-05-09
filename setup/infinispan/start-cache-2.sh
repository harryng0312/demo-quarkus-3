# /bin/sh
#./server.sh -g "test-infinispan-group" -j "pmobile3-stack" -b "0.0.0.0" -Djgroups.tcpping.initial_hosts=localhost[7800],127.0.0.1[7800]
./bin/server.sh -s ./server2 -o 100 -b 0.0.0.0 -g "test-infinispan-group" -j "pmobile3-stack" \
  -Dinfinispan.site.name=site2 \
  -Djgroups.mcast_port=46656 \
  -Djgroups.tcpping.initial_hosts=localhost[7800],127.0.0.1[7900]