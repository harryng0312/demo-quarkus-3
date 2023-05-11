:: ./server.sh -g "test-infinispan-group" -j "pmobile3-stack" -b "0.0.0.0" -Djgroups.tcpping.initial_hosts=localhost[7800],127.0.0.1[7800]
@echo off
SET BIND_ADDR=192.168.1.13
@REM SET BIND_ADDR=127.0.0.1
.\bin\server.bat -s server1 -o 000 -b %BIND_ADDR% -g "test-infinispan-group" -j "pmobile3-stack" -n "node1" ^
  -Djgroups.mcast_port=46656 ^
  -Djgroups.join_timeout=1000 ^
  -Djgroups.tcpping.initial_hosts="%BIND_ADDR%[7800],%BIND_ADDR%[7850],%BIND_ADDR%[7900]"
::  -Dinfinispan.site.name=site1 ^