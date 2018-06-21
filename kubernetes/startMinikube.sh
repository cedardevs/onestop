#!/bin/bash

# Start minikube if not already running
if (( $(minikube status | grep Running | wc -l) != 2 )); then
  minikube start --cpus 4 --memory 8192
fi

# Set docker env
eval $(minikube docker-env)

echo
echo "Minikube running and docker env set"
echo
