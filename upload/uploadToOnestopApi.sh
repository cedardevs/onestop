#!/bin/bash

if [ $# != 2 ]; then
  echo
  echo "Usage: $0 <rootDir> <loadEndpoint>"
  echo
  echo "  rootDir        The base directory to recursively upload xml files from."
  echo "  loadEndpoint   The full URL of the metadata upload endpoint."
  echo "                 Should be structured like: <host>[:port]/<contextPath>/metadata"
  echo "                 e.g. for a locally-running API hosted by tomcat: http://localhost:8080/onestop-api/metadata"
  echo
  exit 1
fi

BASEDIR="$1"
UPLOAD="$2"
UPDATE="${UPLOAD/%metadata/admin/index/search/update}"

while read file; do
  echo "`date` - Uploading $file to $UPLOAD : `curl -sS $UPLOAD -H "Content-Type: application/xml" -d "@$file"`"
done < <(find $BASEDIR -type f -name "*.xml" -print)

echo "`date` - Triggering search index update: `curl -sS $UPDATE`"