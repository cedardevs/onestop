This set of Kubernetes configuration files allows for a deployment of OneStop into a cloud provider (i.e., GCP, AWS, etc.).

In order to supply configuration to the Search API, use the following command:
`kubectl create configmap onestop-search-api-config --from-file=application.yaml`
This must be run BEFORE creating the Search API deployment/service.
