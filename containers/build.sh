#!/bin/bash

function usage {
    echo 'Usage: ./build.sh (dev|test) [keep]' >&2
}

source ./common.sh
db_name "$1"

if [[ -z "$DB_NAME" ]] ; then
    usage
    exit 1
fi

if [[ "$2" != 'keep' ]] ; then
    ./rmi.sh "$1"
fi

docker build -f Dockerfile.postgres -t "$DB_NAME" .
