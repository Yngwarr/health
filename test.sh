#!/bin/bash

set -x

pushd './containers' > /dev/null
./build.sh test keep
./run.sh test keep
popd > /dev/null

clj -M:test "$@"

pushd './containers' > /dev/null
./stop.sh test
./rmi.sh test
popd > /dev/null
