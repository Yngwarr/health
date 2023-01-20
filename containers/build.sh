#!/bin/bash

docker rmi postgres
docker build -f Dockerfile.postgres -t postgres .
