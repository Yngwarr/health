#!/bin/bash

PORT=5432

if [[ -n "$1" ]] ; then
    PORT="$1"
fi

psql -d postgres -h localhost -p "$PORT" -U postgres -W
