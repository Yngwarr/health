#!/bin/bash

function usage {
    echo 'Usage: ./run.sh (dev|test) [keep]' >&2
}

source ./common.sh
db_name "$1"

if [[ -z "$DB_NAME" ]] ; then
    usage
    exit 1
fi

docker stop "$DB_NAME"
docker rm "$DB_NAME"
