#!/bin/bash

clj -M:dev -m figwheel.main -bo prod
clj -M -m health.backend
