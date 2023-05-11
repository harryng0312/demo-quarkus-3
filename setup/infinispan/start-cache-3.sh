# /bin/sh
#./server.sh -g "test-infinispan-group" -j "pmobile3-stack" -b "0.0.0.0" -Djgroups.tcpping.initial_hosts=localhost[7800],127.0.0.1[7800]
export BIND_ADDR=192.168.2.254
./bin/server.sh -s ./server3 -o 100 -b $BIND_ADDR -g "test-infinispan-group" -j "pmobile3-stack" \
  -Dinfinispan.site.name=site2 \
  -Djgroups.mcast_port=46656 \
  -Djgroups.join_timeout=2000 \
  -Djgroups.tcpping.initial_hosts="$BIND_ADDR[7800],$BIND_ADDR[7900]"
#  -Djgroups.gossipAddress=127.0.0.1[17800]
