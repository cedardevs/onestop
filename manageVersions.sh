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
  sed -i -- "s/version: .*/version: $1/g" search/src/main/resources/openapi.yaml
  sed -i -- "s/version: .*/version: $1/g" registry/src/main/resources/openapi_base.yaml
}


getCurrentVersions(){
  echo search/src/main/resources/openapi.yaml ; grep version search/src/main/resources/openapi.yaml
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
