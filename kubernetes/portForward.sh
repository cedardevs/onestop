#!/bin/bash

# Port forwarding of APIs and Kibana	
kubectl port-forward deployments/onestop-api-metadata 8098:8098 > onestop-api-metadata-PF.log 2>&1 &	
echo "api-metadata listening on localhost:8098"	

kubectl port-forward deployments/onestop-api-search 8097:8097 > onestop-api-search-PF.log 2>&1 &	
echo "api-search listening on localhost:8097"	

kubectl port-forward deployments/kibana 5601:5601 > kibana-PF.log 2>&1 &	
echo "kibana listening on localhost:5061"
