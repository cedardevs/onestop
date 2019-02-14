#!/usr/bin/env bash

BASEDIR=$(dirname "$0")

pushd "$BASEDIR/psi" > /dev/null
helm package .
popd  > /dev/null

pushd "$BASEDIR"  > /dev/null
helm repo index . --merge index.yaml
echo "Updated index.yaml"
popd  > /dev/null

exit 0
