#!/bin/bash

function usage {
    echo 'Usage: ./rmi.sh (dev|test)' >&2
}

source ./common.sh
db_name "$1"

if [[ -z "$DB_NAME" ]] ; then
    usage
    exit 1
fi

docker rmi "$DB_NAME"
