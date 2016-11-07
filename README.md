
# Cakeshop: The Enterprise Blockchain SDK

All the tools you need to build apps on blockchain

![screenshot](https://github.com/noirqs/cakeshop/blob/opensource/doc/screenshot.png?raw=true "screenshot")

## What is it?

_Cakeshop_ is a set of tools and APIs for working with the
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
* Add Java environment variable -Dspring.profiles.active=local|dev|uat|prod to startup script
* Start app server
* Navigate to [http://localhost:8080/cakeshop/](http://localhost:8080/cakeshop/)

That's it!

### Running via Docker

```
docker run -it -e JAVA_OPTS="-Dspring.profiles.active=local" cakeshop
```

### Building a Docker Image

```
# builder image
docker build -t cakeshop-build docker/build/

# build cakeshop.war
docker run -v ~/.m2:/root/.m2 -v $(pwd):/usr/src -w /usr/src cakeshop-build mvn -DskipTests clean package

# cakeshop image
mv cakeshop-api/target/cakeshop*.war docker/cakeshop/
docker build -t cakeshop docker/cakeshop/

docker run -it -e JAVA_OPTS="-Dspring.profiles.active=local" cakeshop
```

```
docker run -it -e JAVA_OPTS="-Dspring.profiles.active=local" cakeshop
```

Minimum requirements for tomcat (conf/server.xml)

    <Connector port="8080" protocol="HTTP/1.1"
               enableLookups="false"
               maxKeepAliveRequests="-1"
               maxConnections="10000"
               redirectPort="8443"
               connectionTimeout="20000"/>

Currently Cakeshop APP supports Oracle, Postgres, MySQL, HSQL databases. Here are the options how to use external or embedded database(HSQL)

LOCAL, DEV are by default running on embedded HSQL DB. UAT and Prod have Oracle as default database. If you'd like to overwrite it you need
to pass -Dcakeshop.database.vendor=oracle|hsqldb|mysql|postgres
into app server startup script.

- For external database only:
  If you have connection pool set up, add Java environment variable -Dcakeshop.jndi.name=your_connection_pool_jndi_name into app server startup script.
  This option is not for embedded database.

  If you'd like to use direct JDBC connection then following Java environment variables have to be added
to start up script -Dcakeshop.jdbc.url=url -Dcakeshop.jdbc.user=db_user -Dcakeshop.jdbc.pass=db_password

- For embedded database:
  Just pass -Dcakeshop.database.vendor=hsqldb into app server start up script.

- If you'd like to overwrite default hibernate settings, following properties have to be added to the app server startup script -Dcakeshop.hibernate.jdbc.batch_size=value -Dcakeshop.hibernate.hbm2ddl.auto=value   -Dcakeshop.hibernate.dialect=value
