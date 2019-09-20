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
  sed -i -- "s/version: .*/version: $1/g" search/schema/openapi.yml
  sed -i -- "s/appVersion:.*/appVersion: \"$1\"/" helm/onestop/Chart.yaml
  sed -i -- "s/  tag:.*/  tag: $1/" helm/onestop-admin/values.yaml
  sed -i -- "s/  tag:.*/  tag: $1/" helm/onestop-search/values.yaml
  sed -i -- "s/  tag:.*/  tag: $1/" helm/onestop-user/values.yaml
  sed -i -- "s/  tag:.*/  tag: $1/" helm/onestop-client/values.yaml
}


getCurrentVersions(){
  echo gradle.properties ; grep version gradle.properties
  echo client/package.json ; grep version client/package.json
  echo search/schema/openapi.yml ; grep version onestop-search/schema/openapi.yml
  echo helm/onestop/Chart.yaml ; grep appVersion helm/onestop/Chart.yaml
  echo helm/onestop-admin/values.yaml ; grep tag helm/onestop-admin/values.yaml
  echo helm/onestop-search/values.yaml ; grep tag helm/onestop-search/values.yaml
  echo helm/onestop-user/values.yaml ; grep tag helm/onestop-user/values.yaml
  echo helm/onestop-client/values.yaml ; grep tag helm/onestop-client/values.yaml
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
