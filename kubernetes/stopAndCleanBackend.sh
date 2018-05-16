#!/bin/bash

while read file; do
  kubectl delete -f $file
done < <(find deployments/ -type f -name "*.yaml" -print)

minikube stop
