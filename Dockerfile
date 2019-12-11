FROM java:8
WORKDIR /target
ADD target /target/
EXPOSE 8080
CMD java -jar getting-started-1.0-SNAPSHOT-runner.jar 
