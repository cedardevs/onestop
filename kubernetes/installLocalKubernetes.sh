#!/bin/bash

# Ensure kubectl and minikube are present
echo
echo "Checking for kubectl and minikube on system"
echo
kubectl >/dev/null 2>&1
if [ $? -gt 0 ]; then
  echo "Installing kubectl. You will be prompted for a password."
  curl -Lo kubectl http://storage.googleapis.com/kubernetes-release/release/v1.10.1/bin/darwin/amd64/kubectl && chmod +x kubectl && sudo mv kubectl /usr/local/bin/
else
  echo
  echo "Kubectl already installed on machine."
  echo
fi

minikube status >/dev/null 2>&1
if [ $? -gt 0 ]; then
  echo "Installing minikube. You will be prompted for a password."
  curl -Lo minikube https://storage.googleapis.com/minikube/releases/v0.26.1/minikube-darwin-amd64 && chmod +x minikube && sudo mv minikube /usr/local/bin/
  # Start minikube with enough CPUs & memory to run the apps. This must be done as initial configuration of cluster
  minikube start --cpus 4 --memory 8192
  echo
  echo "Minikube VM installed, configured, and running."
else
  echo
  echo "Minikube already installed on machine."
fi
