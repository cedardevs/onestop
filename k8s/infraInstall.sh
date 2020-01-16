#!/bin/bash

kubectl apply -f https://download.elastic.co/downloads/eck/1.0.0-beta1/all-in-one.yaml
kubectl apply -f ./elasticsearch.yaml
