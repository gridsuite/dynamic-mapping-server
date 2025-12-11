#!/bin/bash

source bearer.sh

url="http://localhost:5036/models"

for json in `ls -l *.json | awk '{print $(NF)}'`; do
   curl -X 'POST' \
   "${url}/" \
   -H "authorization: Bearer ${bearer}" \
   -H 'Content-Type: application/json' \
   -d @${json}
done

for json in `ls -l variables/*.json | awk '{print $(NF)}'`; do
   curl -X 'POST' \
   "${url}/variables-sets" \
   -H "authorization: Bearer ${bearer}" \
   -H 'Content-Type: application/json' \
   -d @${json}
done
