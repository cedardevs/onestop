#!/bin/bash

if [[ $# != 4 ]]; then
  echo "Usage: $0 username apiKey buildNumber releaseVersion"
  exit 1
fi

curl -X POST -u "$1:$2" -H "Content-Length: 0" "http://oss.jfrog.org/api/plugins/build/promote/snapshotsToBintray/onestop/$3?params=releaseVersion=$4"
