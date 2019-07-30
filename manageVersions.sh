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
  sed -i -- "s/\"version\":.*/\"version\": \"$1\",/g" client/package.json
  sed -i -- "s/VERSION:.*/VERSION: $1/g" skaffold.yaml
  sed -i -- "s/version: .*/version: $1/g" api-search/schema/openapi.yml
  sed -i -- "s/appVersion:.*/appVersion: \"$1\"/" helm/onestop/Chart.yaml
  sed -i -- "s/  tag:.*/  tag: $1/" helm/api-admin/values.yaml
  sed -i -- "s/  tag:.*/  tag: $1/" helm/api-search/values.yaml
  sed -i -- "s/  tag:.*/  tag: $1/" helm/api-user/values.yaml
  sed -i -- "s/  tag:.*/  tag: $1/" helm/client/values.yaml
}


getCurrentVersions(){
  echo gradle.properties ; grep version gradle.properties
  echo client/package.json ; grep version client/package.json
  echo skaffold.yaml ; grep VERSION skaffold.yaml
  echo api-search/schema/openapi.yml ; grep version api-search/schema/openapi.yml
  echo helm/onestop/Chart.yaml ; grep appVersion helm/onestop/Chart.yaml
  echo helm/api-admin/values.yaml ; grep tag helm/api-admin/values.yaml
  echo helm/api-search/values.yaml ; grep tag helm/api-search/values.yaml
  echo helm/api-user/values.yaml ; grep tag helm/api-user/values.yaml
  echo helm/client/values.yaml ; grep tag helm/client/values.yaml
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
