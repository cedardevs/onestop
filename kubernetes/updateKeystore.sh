#!/bin/bash

KEYSTORE_FILE="./keystore.jks"

BLUE=$(tput setaf 4)
NORMAL=$(tput sgr0)

# make temporary directory to work in
TMP_DIR="$(mktemp -d)"

# move into temporary directory
pushd "${TMP_DIR}"

# clone help repository to get latest keystore
git clone https://github.com/cedardevs/help.git

# return to original script directory
popd

# extract keystore file and cleanup clone from temporary directory
cp "${TMP_DIR}/help/keystores/sciapps.colorado.edu.jks" "${KEYSTORE_FILE}"
rm -rf "${TMP_DIR}"

# prompt user for keystore password
printf "\n${BLUE}Enter the sciapps.colorado.edu.jks keystore password:${NORMAL}\n"
KEYSTORE_PASSWORD="$( stty -echo; head -n 1; stty echo )"

KEYSTORE_ALIAS=sciapps.colorado.edu
KEY_PASSWORD=""

echo "KEYSTORE_PASSWORD=${KEYSTORE_PASSWORD}"
echo "KEYSTORE_ALIAS=${KEYSTORE_ALIAS}"
echo "KEY_PASSWORD=${KEY_PASSWORD}"

# delete old secrets
kubectl delete secret keystore

# keystore file and env secrets
kubectl create secret generic keystore \
  --from-file="${KEYSTORE_FILE}" \
  --from-literal=keystore_password="${KEYSTORE_PASSWORD}" \
  --from-literal=keystore_alias="${KEYSTORE_ALIAS}" \
  --from-literal=key_password="${KEY_PASSWORD}"

# remove keystore after secret is created
rm "${KEYSTORE_FILE}"
