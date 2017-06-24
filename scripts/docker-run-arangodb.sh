#!/usr/bin/env bash
echo "ArangoDB will listen on localhost:8529"
docker run -it -e ARANGO_NO_AUTH=1 -p 8529:8529 arangodb:3.1
