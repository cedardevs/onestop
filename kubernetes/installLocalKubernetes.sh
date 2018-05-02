#!/bin/bash

if [ $# != 2 ]; then
  echo
  echo "Usage: $0 <kubectlVersion> <minikubeVersion>"
  echo
  exit 1
fi

# Ensure kubectl and minikube are present
echo
echo "Checking for kubectl and minikube on system"
echo
kubectl version
if [ $? -gt 0 ]; then
  echo "Installing kubectl. You will be prompted for a password."
  curl -Lo kubectl http://storage.googleapis.com/kubernetes-release/release/$1/bin/darwin/amd64/kubectl && chmod +x kubectl && sudo mv kubectl /usr/local/bin/
else
  echo
  echo "Kubectl already installed on machine."
  echo
fi

minikube status
if [ $? -gt 0 ]; then
  echo "Installing minikube. You will be prompted for a password."
  curl -Lo minikube https://storage.googleapis.com/minikube/releases/$2/minikube-darwin-amd64 && chmod +x minikube && sudo mv minikube /usr/local/bin/
  # Start minikube with enough CPUs & memory to run the apps. This must be done as initial configuration of cluster
  minikube start --cpus 4 --memory 8192
  echo
  echo "Minikube VM installed, configured, and running."
else
  echo
  echo "Minikube already installed on machine."
fi
