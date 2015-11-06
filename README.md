1. Clone develop branch
2. Set your tomcat by adding -Deth.environment=local JVM argument
3. Update your get start up script by adding this --rpc --rpcport 8102 --rpcapi "admin,db,eth,debug,miner,net,shh,txpool,personal,web3" 
(this is temporary until we develop utility to start it from within webapp).
4. You good to go.
