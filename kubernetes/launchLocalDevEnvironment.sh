#!/bin/bash

if [ $# != 2 ]; then
  echo
  echo "Usage: $0 <kubectlVersion> <minikubeVersion>"
  echo
  echo "  Warning: This script will continue to execute without the version args, however,"
  echo "     if either kubectl or minikube is not already installed locally, it will immediately"
  echo "     fail."
  echo
fi

#

# Ensure kubectl and minikube are present
echo "Checking for kubectl and minikube on system"
kubectl version
KC_STATUS=`echo $?`
minikube status
MK_STATUS=`echo $?`

if [ KC_STATUS -gt 0 ]; then
  echo "Installing kubectl. You will be prompted for a password."
  curl -Lo kubectl http://storage.googleapis.com/kubernetes-release/release/v$1/bin/darwin/amd64/kubectl && chmod +x kubectl && sudo mv kubectl /usr/local/bin/
fi

if [ MK_STATUS -gt 0 ]; then
  echo "Installing minikube. You will be prompted for a password."
  curl -Lo minikube https://storage.googleapis.com/minikube/releases/v$2/minikube-darwin-amd64 && chmod +x minikube && sudo mv minikube /usr/local/bin/
fi

# Start minikube with enough CPUs & memory to run the apps. This must be done as initial configuration of cluster
minikube start --cpus 4 --memory 8192

# Set docker env
eval $(minikube docker-env)

# Build docker resources
./../gradlew build

# Create kubernetes deployments for elasticsearch, kibana, and both OneStop APIs
while read file; do
  kubectl create -f $file
done < <(find deployments/ -type f -name "*.yaml" -print)

echo "Waiting 15s for services to start up..."
sleep 15s

# Port forwarding of APIs and Kibana
kubectl port-forward deployment/onestop-api-metadata 8098:8098 > onestop-api-metadata-PF.log 2>&1 &
echo "api-metadata listening on localhost:8098"
kubectl port-forward deployment/onestop-api-search 8097:8097 > onestop-api-search-PF.log 2>&1 &
echo "api-search listening on localhost:8097"
kubectl port-forward deployment/kibana 5601:5601 > kibana-PF.log 2>&1 &
echo "kibana listening on localhost:5061"

# Load test data
./../gradlew uploadTestData

# Info in case issues arise with minikube
echo "Installation and deployment completed."
echo
echo "NOTE:"
echo "  If minikube cluster is continually crashing, you may need to run 'minikube delete' to"
echo "  remove existing VM, then 'minikube start --cpus 4 --memory 8192' to create a more robust VM."
echo "  Re-execute this script to re-deploy the OneStop environment afterward."
