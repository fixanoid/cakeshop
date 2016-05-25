
# Enterprise Blockchain SDK

All the tools you need to build apps on blockchain

![screenshot](https://stash-prod6.us.jpmchase.net:8443/projects/NPD/repos/ethereum-enterprise/browse/src/main/webapp/img/readme/sdk.png?raw "screenshot")

## What is it?

The _SDK_ is a set of tools and APIs for working with the
[Ethereum](https://ethereum.org/) blockchain, packaged as a Java web application
archive (WAR) that can be dropped into any application server and gets you up
and running in under 60 seconds.

Included in the package is the [geth](https://github.com/ethereum/go-ethereum)
Ethereum server, a [Solidity](https://solidity.readthedocs.org/en/latest/)
compiler and all dependencies.

It provides tools for managing a local blockchain node, setting up clusters,
exploring the state of the chain, and working with contracts. There are also
APIs for directly integrating applications with the chain.

## Quickstart

### Requirements

* Java 7+
* Java app server (Tomcat, Jetty, etc)

### Installation

* Download WAR file
* Put in `/webapps` folder of your app server
* Start app server
* Navigate to [http://localhost:8080/cakeshop/](http://localhost:8080/cakeshop/)

That's it!
