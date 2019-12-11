# Hazelcast and Quarkus

This guide helps you start with Hazelcast in Quarkus based Microservice and deploy to Kubernetes. 
Feel free to fork and experiment on your own

## Requirements

### Kubernetes Cluster
In this guide, I used a Google Kubernetes Engine but you can use any Kubernetes cluster you choose.
```
$ gcloud container clusters create hazelcast-quarkus-k8s-cluster --cluster-version=1.14.8-gke.12 --num-nodes=4
```

### Hazelcast RBAC 

RBAC is needed by [hazelcast-kubernetes](https://github.com/hazelcast/hazelcast-kubernetes) plugin discovery.
```
$ kubectl apply -f rbac.yaml
```

## Hazelcast-Quarkus Code Sample

This guide contains a basic Quarkus Microservice with Hazelcast Client Server Topology. 
Business Logic in both examples are the same to keep it simple. `put` operation puts a key-value pair to Hazelcast and `get` operation returns the value together with the Kubernetes Pod Name. PodName is used to show that the value is returned from any Pod inside the Kubernetes cluster to prove the true nature of Distributed Cache.

### Hazelcast Client Server

Client-Server code sample can be built and pushed to your own Docker Hub or some other registry via following command but that is optional.
If you decide to build your own image then you should update `hazelcast-quarkus-client.yaml` file with `YOUR-NAME/hazelcast-quarkus-kubernetes` as a new image.
```
$ docker build . -t YOUR-NAME/hazelcast-quarkus-kubernetes
$ docker login
$ docker push YOUR-NAME/hazelcast-quarkus-kubernetes
```
Pre-built image used in this guide is located [here](https://hub.docker.com/r/mesut/hazelcast-quarkus-kubernetes)

Deploy Hazelcast Cluster
```
kubectl apply -f hazelcast-cluster.yaml
```

You can see that 3 member cluster has been initiated with 3 pods.

```
$ kubectl get pods
NAME                  READY   STATUS    RESTARTS   AGE
hazelcast-cluster-0   1/1     Running   0          3h49m
hazelcast-cluster-1   1/1     Running   0          3h49m
hazelcast-cluster-2   1/1     Running   0          3h49m
```

Deploy Quarkus Microservice with Hazelcast Client
```
kubectl apply -f hazelcast-quarkus-client.yaml
```
Check logs and see that quarkus is connected to the cluster.

```
$ kubectl logs hazelcast-client-0 hazelcast-client -f
...
Members [3] {
	Member [10.16.2.14]:5701 - 51274b4d-dc7f-4647-9ceb-c32bfc922c95
	Member [10.16.1.15]:5701 - 465cfefa-9b26-472d-a204-addf3b82d40a
	Member [10.16.2.15]:5701 - 67fdf27a-e7b7-4ed7-adf1-c00f785d2325
}
...
```


let's run a container with curl installed and set an environment variable to point to Load Balancer.

Launch a curl container inside kubernetes cluster and set service IP as environment variable
```
$ kubectl run curl --rm --image=radial/busyboxplus:curl -i --tty
```

Put a value to the cluster
```
$ curl "http:/quarkus-service/put?key=1&value=2"
{"value":"2","podName":"hazelcast-embedded-2"}
```
Get the value from cluster in a loop and see that it is retrieved from different Pod Names.
```
$ while true; do curl "http:/quarkus-service/get?key=1"; sleep 2;echo; done
{"value":"2","podName":"hazelcast-cluster-1"}
{"value":"2","podName":"hazelcast-cluster-0"}
...
```
In this sample, you were able to deploy a quarkus based microservice with hazelcast client-server topology and deployed to Kubernetes. Let's clean up deployments with the following command.

```
$ kubectl delete -f hazelcast-quarkus-client.yaml
$ kubectl delete -f hazelcast-cluster.yaml
$ kubectl delete -f rbac.yaml
```
