#!/bin/bash

#remove all Elastic resources in all namespaces:

kubectl get namespaces --no-headers -o custom-columns=:metadata.name \
  | xargs -n1 kubectl delete elastic --all -n

#This deletes all underlying Elasticsearch, Kibana, and APM Server resources (pods, secrets, services, etc.).
#Then, you can uninstall the operator:

kubectl delete -f https://download.elastic.co/downloads/eck/1.0.0/all-in-one.yaml
