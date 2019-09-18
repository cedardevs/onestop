#!/bin/bash

if [ $# != 2 ]; then
  echo
  echo "Usage: $0 <rootDir> <loadEndpoint>"
  echo
  echo "  rootDir        The base directory to recursively upload xml files from."
  echo "  loadEndpoint   The full URL of the metadata upload endpoint."
  echo "                 Should be structured like: <host>[:port]/<contextPath>/metadata"
  echo "                 e.g. for a locally-running API hosted by tomcat: http://localhost:8080/onestop-admin/metadata"
  echo
  exit 1
fi

RED='\033[0;31m'
GREEN='\033[0;32m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

BASEDIR="$1"
UPLOAD="$2"
UPDATE="${UPLOAD/%metadata/admin/index/search/update}"

FILE_COUNT=0
SUCCESS_COUNT=0

echo -e "${BLUE}===========================================${NC}"
echo -e "${BLUE}Uploading files to ${UPLOAD}${NC}"
echo -e "${BLUE}===========================================${NC}"
while read file; do
  responseLabel="${RED}FAILED${NC}"
  curl -sS -o /dev/null $UPLOAD -H "Content-Type: application/xml" -d "@$file"
  if [ 0 -eq $? ]; then
    ((++SUCCESS_COUNT))
    responseLabel="${GREEN}SUCCESS${NC}"
  fi;
  echo -e "$(date -u +%FT%TZ) [$((++FILE_COUNT))] ${responseLabel} -> ${file}"
done < <(find ${BASEDIR} -type f -name "*.xml" -print)

if [ "${SUCCESS_COUNT}" -eq "${FILE_COUNT}" ]; then
    echo -e "${BLUE}===========================================${NC}"
    echo -e "${GREEN}UPLOAD SUCCESS  : Uploaded all ${FILE_COUNT} XML files.${NC}"
else
    echo -e "${BLUE}===========================================${NC}"
    echo -e "${RED}UPLOAD FAILED   : Only uploaded ${SUCCESS_COUNT} out of ${FILE_COUNT} XML files.${NC}"
fi;

# redirect curl output to file and wait for the file to be closed
# this is a bit awkward but seems to work and is better than blindly
# "succeeding" when curl came back with an exit status = 0 when the
# response could potentially not be '{"acknowledged":true}'
rm curl.output
RETRIES=0
MAX_RETRIES=10
curl -sS ${UPDATE} > curl.output
CURL_EXIT=$?
until [[ "$( find /proc/*/fd 2> /dev/null |
   xargs -I{} readlink {} |
   grep -c '^curl\.output$' )" == "0" ]]; do
   if [[ "${RETRIES}" -gt "${MAX_RETRIES}" ]]; then
        break;
   fi;
   sleep 1;
   ((RETRIES++))
   echo "Seconds waited for index update acknowledgement: ${RETRIES}..."
done;


CURL_OUTPUT="$(cat curl.output | awk '{$1=$1;print}')"
if [[ ${CURL_OUTPUT} == '{"acknowledged":true}' ]]; then
    echo -e "${GREEN}INDEXING SUCCESS: Search Index Updated ->${NC} ${CURL_OUTPUT}"
    echo -e "${BLUE}===========================================${NC}"
else
    echo -e "${RED}INDEXING FAILED : Could not update search index${NC}"
    echo -e "${BLUE}===========================================${NC}"
fi;


if [[ "${SUCCESS_COUNT}" -ne "${FILE_COUNT}" ]] || [[ ${CURL_OUTPUT} != '{"acknowledged":true}' ]]; then
    exit 1
fi;
