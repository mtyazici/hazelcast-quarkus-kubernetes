
# RUNNING FROM JAR 

## Package the app
> $ mvn clean package   

## Create Hazelcast Instance
> $ mvn exec:java -Dexec.mainClass=org.acme.quickstart.HzInstance

Note the ip and port of the instance here (e.g. 10.216.1.29:5701)

## Run Quarkus App
> $ java -jar target/getting-started-1.0-SNAPSHOT-runner.jar   
 
## Request to App
After sending the request below, go back to Quarkus tab and enter the address of the instance there. The response will then be returned to the curl request.

> $ curl "localhost:8080/map/put?key=key_1&value=value_1"   
(on Quarkus tab) Enter cluster address: 10.216.1.29:5701

## Get the value from IMap
> $ curl "localhost:8080/map/get?key=key_1"   


# BUILDING NATIVE IMAGE
See prerequisites here: https://quarkus.io/guides/building-native-image-guide    
Docker must be running.

--------

> $ mvn package -Pnative -Dnative-image.docker-build=true

------------------------------------

# Dockerfile content:

Dockerfile.native

```
FROM registry.access.redhat.com/ubi8/ubi-minimal
WORKDIR /work/
COPY target/*-runner /work/application
RUN chmod 775 /work
EXPOSE 8080
CMD ["./application", "-Dquarkus.http.host=0.0.0.0"]
```


# RUNNING IN CONTAINER
First, start Hazelcast Cluster in container:
> $ docker run -p 5701:5701 hazelcast/hazelcast:3.12.1
> $ docker run -p 5702:5701 hazelcast/hazelcast:3.12.1

Start Quarkus App:
> $ docker build -f Dockerfile.native -t quarkus-quickstart/getting-started .   
> $ docker run -i -p 8080:8080 quarkus-quickstart/getting-started

Make a request:
> $ curl "localhost:8080/map/get?key=1"

Navigate back to the terminal tab where Quarkus is running. It will prompt for an IP adress which is the client's networking IP. Enter the cluster IP (seen on Hazelcast Cluster log) there.
> Enter networking address: 172.17.0.3:5701

Now check the `curl` tab and see the response, which must be `null` since no value has been put yet.
So Hazelcast Client in native-image works properly when the configurations below which cause build time error are set in pom.xml :

```
<plugin>
  <groupId>io.quarkus</groupId>
  <artifactId>quarkus-maven-plugin</artifactId>
  <version>${quarkus.version}</version>
  <executions>
    <execution>
      <goals>
        <goal>native-image</goal>
      </goals>
      <configuration>
        <reportErrorsAtRuntime>true</reportErrorsAtRuntime>
        <enableHttpUrlHandler>true</enableHttpUrlHandler>
        <enableJni>true</enableJni>
      </configuration>
    </execution>
  </executions>
</plugin>
```



