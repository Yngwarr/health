#!/bin/bash

docker stop postgres
docker rm postgres

docker run -e POSTGRES_PASSWORD=deathstar --name postgres -dp 5432:5432 postgres
