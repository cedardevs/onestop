{{/* vim: set filetype=mustache: */}}
{{/*
Expand the name of the chart.
*/}}
{{- define "onestop-parsalyzer.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Create a default fully qualified app name.
We truncate at 63 chars because some Kubernetes name fields are limited to this (by the DNS naming spec).
If release name contains chart name it will be used as a full name.
*/}}
{{- define "onestop-parsalyzer.fullname" -}}
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
{{- define "onestop-parsalyzer.chart" -}}
{{- printf "%s-%s" .Chart.Name .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Common labels
*/}}
{{- define "onestop-parsalyzer.labels" -}}
helm.sh/chart: {{ include "onestop-parsalyzer.chart" . }}
{{ include "onestop-parsalyzer.selectorLabels" . }}
{{- if .Chart.AppVersion }}
app.kubernetes.io/version: {{ .Chart.AppVersion | quote }}
{{- end }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- end }}

{{/*
Selector labels
*/}}
{{- define "onestop-parsalyzer.selectorLabels" -}}
app.kubernetes.io/name: {{ include "onestop-parsalyzer.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end }}

{{/*
Create a default fully qualified kafka headless name.
We truncate at 63 chars because some Kubernetes name fields are limited to this (by the DNS naming spec).
*/}}
{{- define "onestop-parsalyzer.cp-kafka-headless.fullname" -}}
{{- $name := default "cp-kafka-headless" (index .Values "kafka" "serviceNameOverride") -}}
{{- printf "%s-%s" .Release.Name $name | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Form the Kafka URL. If Kafka is installed as part of this chart, use k8s service discovery,
else use user-provided URL
*/}}
{{- define "onestop-parsalyzer.kafka.bootstrap.servers" -}}
{{- if .Values.kafka.bootstrap.servers -}}
{{- .Values.kafka.bootstrap.servers -}}
{{- else -}}
{{- printf "PLAINTEXT://%s:9092" (include "onestop-parsalyzer.cp-kafka-headless.fullname" .) -}}
{{- end -}}
{{- end -}}

{{/*
Create a default fully qualified schema registry name.
We truncate at 63 chars because some Kubernetes name fields are limited to this (by the DNS naming spec).
*/}}
{{- define "onestop-parsalyzer.cp-schema-registry.fullname" -}}
{{- $name := default "cp-schema-registry" (index .Values "cp-schema-registry" "serviceNameOverride") -}}
{{- printf "%s-%s" .Release.Name $name | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Form the Schema Registry URL. If Schema Registry is installed as part of this chart, use k8s service discovery,
else use user-provided URL
*/}}
{{- define "onestop-parsalyzer.kafka.schema.registry.url" -}}
{{- if .Values.kafka.schema.registry.url -}}
{{- printf "%s" .Values.kafka.schema.registry.url -}}
{{- else -}}
{{- printf "http://%s:8081" (include "onestop-parsalyzer.cp-schema-registry.fullname" .) -}}
{{- end -}}
{{- end -}}
