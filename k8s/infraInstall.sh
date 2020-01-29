#!/bin/bash

kubectl apply -f https://download.elastic.co/downloads/eck/1.0.0/all-in-one.yaml

# workaround to customize the bootstrap password BEFORE creating the Elastic resources
# as the operator currently doesn't support configuring the bootstrap password declaratively
PASSWORD="foamcat"
kubectl create secret generic onestop-es-elastic-user --from-literal=elastic="${PASSWORD}"

kubectl apply -f ../k8s/es_kibana_apm.yaml

