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

case "$1" in
    dev) PORT=5432 ;;
    test) PORT=5555 ;;
esac

if [[ "$2" != 'keep' ]] ; then
    docker stop "$DB_NAME"
    docker rm "$DB_NAME"
fi

# TODO load password from a config file or environment
docker run -e POSTGRES_PASSWORD=deathstar --name "$DB_NAME" -dp $PORT:5432 "$DB_NAME"
