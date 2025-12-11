#!/bin/bash

source bearer.sh

url="http://localhost:5036/models"

curl -X 'PATCH' "${url}/GeneratorSynchronousThreeWindings/variables-sets/add" -H "authorization: Bearer ${bearer}" -H 'Content-Type: application/json' -d '["SynchronousGenerator"]'
curl -X 'PATCH' "${url}/GeneratorSynchronousFourWindings/variables-sets/add" -H "authorization: Bearer ${bearer}" -H 'Content-Type: application/json' -d '["SynchronousGenerator"]'
curl -X 'PATCH' "${url}/GeneratorSynchronousThreeWindingsProportionalRegulations/variables-sets/add" -H "authorization: Bearer ${bearer}" -H 'Content-Type: application/json' -d '["SynchronousGenerator", "VoltageRegulatorProportional"]'
curl -X 'PATCH' "${url}/GeneratorSynchronousFourWindingsProportionalRegulations/variables-sets/add" -H "authorization: Bearer ${bearer}" -H 'Content-Type: application/json' -d '["SynchronousGenerator", "VoltageRegulatorProportional"]'
curl -X 'PATCH' "${url}/GeneratorPQ/variables-sets/add" -H "authorization: Bearer ${bearer}" -H 'Content-Type: application/json' -d '["GeneratorPQ"]'
curl -X 'PATCH' "${url}/GeneratorPV/variables-sets/add" -H "authorization: Bearer ${bearer}" -H 'Content-Type: application/json' -d '["GeneratorPV"]'