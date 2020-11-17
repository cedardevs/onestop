#!/bin/bash

# install Elastic Cloud on Kubernetes CRDs
kubectl apply -f https://download.elastic.co/downloads/eck/1.1.0/all-in-one.yaml

# add helm repos
helm repo add stable https://charts.helm.sh/stable
helm repo add confluent https://confluentinc.github.io/cp-helm-charts/
helm repo add ingress-nginx https://kubernetes.github.io/ingress-nginx
helm repo add bitnami https://charts.bitnami.com/bitnami
helm repo add runix https://helm.runix.net

# update helm dependencies for the main onestop chart
pushd "$(dirname $0)/onestop"
helm dependency update
popd

# update helm dependencies for the dev support chart
pushd "$(dirname $0)/onestop-dev"
helm dependency update
popd
