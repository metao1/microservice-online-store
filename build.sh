#!/usr/bin/env bash

gradle order-microservice:build -x test

docker-compose  -f docker-compose.yml up --build -d