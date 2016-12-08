# Building

## Building a Docker Image

```
# builder image
docker build -t cakeshop-build docker/build/

# build cakeshop.war
docker run -v ~/.m2:/root/.m2 -v $(pwd):/usr/src -w /usr/src cakeshop-build mvn -DskipTests clean package

# cakeshop image
mv cakeshop-api/target/cakeshop*.war docker/cakeshop/
docker build -t cakeshop docker/cakeshop/
```
