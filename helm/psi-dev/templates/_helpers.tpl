{{/* vim: set filetype=mustache: */}}
{{/*
Expand the name of the chart.
*/}}
{{- define "psi-dev.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Create a default fully qualified app name.
We truncate at 63 chars because some Kubernetes name fields are limited to this (by the DNS naming spec).
If release name contains chart name it will be used as a full name.
*/}}
{{- define "psi-dev.fullname" -}}
{{- if .Values.fullnameOverride -}}
{{- .Values.fullnameOverride | trunc 63 | trimSuffix "-" -}}
{{- else -}}
{{- $name := default .Chart.Name .Values.nameOverride -}}
{{- if contains $name .Release.Name -}}
{{- .Release.Name | trunc 63 | trimSuffix "-" -}}
{{- else -}}
{{- printf "%s-%s" .Release.Name $name | trunc 63 | trimSuffix "-" -}}
{{- end -}}
{{- end -}}
{{- end -}}

{{/*
Create chart name and version as used by the chart label.
*/}}
{{- define "psi-dev.chart" -}}
{{- printf "%s-%s" .Chart.Name .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Create a default fully qualified zookeeper service name.
We truncate at 63 chars because some Kubernetes name fields are limited to this (by the DNS naming spec).
*/}}
{{- define "psi-dev.zookeeper.fullname" -}}
{{- $name := default "cp-zookeeper" (index .Values "zookeeper" "serviceNameOverride") -}}
{{- printf "%s-%s" .Release.Name $name | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Form the zookeeper URL. If Kafka is installed as part of this chart, use k8s service discovery,
else use user-provided URL
*/}}
{{- define "psi-dev.zookeeper.url" -}}
{{- if .Values.zookeeper.url -}}
{{- .Values.zookeeper.url -}}
{{- else -}}
{{- printf "%s:2181" (include "psi-dev.zookeeper.fullname" .) -}}
{{- end -}}
{{- end -}}

{{/*
Create a default fully qualified kafka-rest service name.
We truncate at 63 chars because some Kubernetes name fields are limited to this (by the DNS naming spec).
*/}}
{{- define "psi-dev.cp-kafka-rest.fullname" -}}
{{- $name := default "cp-kafka-rest" (index .Values "kafka-rest" "serviceNameOverride") -}}
{{- printf "%s-%s" .Release.Name $name | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Form the kafka-rest URL. If Kafka is installed as part of this chart, use k8s service discovery,
else use user-provided URL
*/}}
{{- define "psi-dev.kafka-rest.url" -}}
{{- if (index .Values "kafka-rest" "url") -}}
{{- (index .Values "kafka-rest" "url") -}}
{{- else -}}
{{- printf "%s:8082" (include "psi-dev.cp-kafka-rest.fullname" .) -}}
{{- end -}}
{{- end -}}
