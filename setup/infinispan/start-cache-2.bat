#./server.sh -g "test-infinispan-group" -j "pmobile3-stack" -b "0.0.0.0" -Djgroups.tcpping.initial_hosts=localhost[7800],127.0.0.1[7800]
.\bin\server.bat -s .\server2 -o 100 -b 192.168.1.13 -g "test-infinispan-group" -j "pmobile3-stack" ^
  -Dinfinispan.site.name=site2 ^
  -Djgroups.mcast_port=46656 ^
  -Djgroups.join_timeout=2000 ^
  -Djgroups.tcpping.initial_hosts="192.168.1.13[7800],192.168.1.13[7900]"
#  -Djgroups.gossipAddress=127.0.0.1[17800]
