#!/bin/bash

usage(){
  echo "Usage: $0"
  echo "  This script helps maintain a consistent version number across the project. Use it to GET and SET version numbers."
  echo "  GET - list current version numbers"
  echo "  SET <version-number> - set all version numbers to <version-number>"
  exit 1
}

# update the properties
updateVersions() {
  prevVersion=$(cat gradle.properties | grep 'version=' | sed -e 's/version=//g' )
  sed -i -- "s/version=.*/version=$1/g" gradle.properties
  sed -i -- "s/VERSION:.*/VERSION: $1/g" skaffold.yaml
  sed -i -- "s/version: .*/version: $1/g" registry/src/main/resources/openapi_base.yaml
  sed -i -- "s/appVersion:.*/appVersion: \"$1\"/" helm/psi/Chart.yaml
  sed -i -- "s/appVersion:.*/appVersion: \"$1\"/" helm/psi-dev/Chart.yaml
  sed -i -- "s/appVersion:.*/appVersion: \"$1\"/" helm/psi-registry/Chart.yaml
  sed -i -- "s/appVersion:.*/appVersion: \"$1\"/" helm/psi-manager/Chart.yaml
  sed -i -- "s/  tag:.*/  tag: $1/" helm/psi-registry/values.yaml
  sed -i -- "s/  tag:.*/  tag: $1/" helm/psi-manager/values.yaml
}


getCurrentVersions(){
  echo gradle.properties ; grep version gradle.properties
  echo skaffold.yaml ; grep VERSION skaffold.yaml
  echo registry/src/main/resources/openapi_base.yaml ; grep version registry/src/main/resources/openapi_base.yaml
  echo helm/psi/Chart.yaml ; grep appVersion helm/psi/Chart.yaml
  echo helm/psi-dev/Chart.yaml ; grep appVersion helm/psi-dev/Chart.yaml
  echo helm/psi-registry/Chart.yaml ; grep appVersion helm/psi-registry/Chart.yaml
  echo helm/psi-manager/Chart.yaml ; grep appVersion helm/psi-manager/Chart.yaml
  echo helm/psi-registry/values.yaml ; grep tag helm/psi-registry/values.yaml
  echo helm/psi-manager/values.yaml ; grep tag helm/psi-manager/values.yaml
}


if [[ $# -lt 1 ]]; then
  usage
fi

if [[ $1 == 'GET' ]]; then
  getCurrentVersions
fi

if [[ $1 == 'SET' ]]; then
  if [[ $# -lt 2  ]]; then
    usage
  else
    read  -n 1 -p "Update all locations to version $2? y/n" userConfirmation
    if [[ $userConfirmation == 'y' ]]; then
      updateVersions $2
    fi
  fi
fi
