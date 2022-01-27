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
  #sed -i -- "s/version=.*/version=$1/g" gradle.properties
  #sed -i -- "s/\"version\":.*/\"version\": \"$1\",/g" client/package.json
  #sed -i -- "s/version: .*/version: $1/g" search/src/main/resources/openapi.yaml
  sed -i -- "s/appVersion:.*/appVersion: \"$1\"/" helm/onestop/Chart.yaml
  #sed -i -- "s/  tag:.*/  tag: $1/" helm/onestop-admin/values.yaml
  sed -i -- "s/  tag:.*/  tag: $1/" helm/onestop-client/values.yaml
  sed -i -- "s/  tag:.*/  tag: $1/" helm/onestop-indexer/values.yaml
  sed -i -- "s/appVersion:.*/appVersion: \"$1\"/" helm/onestop-manager/Chart.yaml
  sed -i -- "s/  tag:.*/  tag: $1/" helm/onestop-manager/values.yaml
  sed -i -- "s/appVersion:.*/appVersion: \"$1\"/" helm/onestop-registry/Chart.yaml
  sed -i -- "s/  tag:.*/  tag: $1/" helm/onestop-registry/values.yaml
  sed -i -- "s/  tag:.*/  tag: $1/" helm/onestop-search/values.yaml
  sed -i -- "s/  tag:.*/  tag: $1/" helm/onestop-user/values.yaml
  sed -i -- "s/version: .*/version: $1/g" registry/src/main/resources/openapi_base.yaml
}


getCurrentVersions(){
# echo gradle.properties ; grep version gradle.properties
# echo client/package.json ; grep version client/package.json
# echo search/src/main/resources/openapi.yaml ; grep version search/src/main/resources/openapi.yaml
  echo helm/onestop/Chart.yaml ; grep appVersion helm/onestop/Chart.yaml
# echo helm/onestop-admin/values.yaml ; grep tag helm/onestop-admin/values.yaml
  echo helm/onestop-client/values.yaml ; grep tag helm/onestop-client/values.yaml
  echo helm/onestop-indexer/values.yaml ; grep tag helm/onestop-indexer/values.yaml
  echo helm/onestop-manager/Chart.yaml ; grep appVersion helm/onestop-manager/Chart.yaml
  echo helm/onestop-manager/values.yaml ; grep tag helm/onestop-manager/values.yaml
  echo helm/onestop-registry/Chart.yaml ; grep appVersion helm/onestop-registry/Chart.yaml
  echo helm/onestop-registry/values.yaml ; grep tag helm/onestop-registry/values.yaml
  echo helm/onestop-search/values.yaml ; grep tag helm/onestop-search/values.yaml
  echo helm/onestop-user/values.yaml ; grep tag helm/onestop-user/values.yaml
  echo registry/src/main/resources/openapi_base.yaml ; grep version registry/src/main/resources/openapi_base.yaml
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
