{{/* vim: set filetype=mustache: */}}
{{/*
Expand the name of the chart.
*/}}
{{- define "onestop-dev.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Create a default fully qualified app name.
We truncate at 63 chars because some Kubernetes name fields are limited to this (by the DNS naming spec).
If release name contains chart name it will be used as a full name.
*/}}
{{- define "onestop-dev.fullname" -}}
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
{{- define "onestop-dev.chart" -}}
{{- printf "%s-%s" .Chart.Name .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Filebeat common labels
*/}}
{{- define "onestop-dev.labels.filebeat" -}}
helm.sh/chart: {{ include "onestop-dev.chart" . }}
{{ include "onestop-dev.selectorLabels.filebeat" . }}
{{- if .Chart.AppVersion }}
app.kubernetes.io/version: {{ .Chart.AppVersion | quote }}
{{- end }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- end }}

{{/*
Filebeat selector labels
*/}}
{{- define "onestop-dev.selectorLabels.filebeat" -}}
app.kubernetes.io/name: {{ include "onestop-dev.name" . }}-filebeat
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end }}

{{/*
Metricbeat common labels
*/}}
{{- define "onestop-dev.labels.metricbeat" -}}
helm.sh/chart: {{ include "onestop-dev.chart" . }}
{{ include "onestop-dev.selectorLabels.metricbeat" . }}
{{- if .Chart.AppVersion }}
app.kubernetes.io/version: {{ .Chart.AppVersion | quote }}
{{- end }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- end }}

{{/*
Metricbeat selector labels
*/}}
{{- define "onestop-dev.selectorLabels.metricbeat" -}}
app.kubernetes.io/name: {{ include "onestop-dev.name" . }}-metricbeat
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end }}
