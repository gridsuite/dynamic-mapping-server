# Dynamic Mapping Server

[![Actions Status](https://github.com/gridsuite/dynamic-mapping-server/actions/workflows/build.yml/badge.svg?branch=main)](https://github.com/gridsuite/dynamic-mapping-server/actions)
[![Coverage Status](https://sonarcloud.io/api/project_badges/measure?project=org.gridsuite%3Adynamic-mapping-server&metric=coverage)](https://sonarcloud.io/component_measures?id=org.gridsuite%3Adynamic-mapping-server&metric=coverage)
[![MPL-2.0 License](https://img.shields.io/badge/license-MPL_2.0-blue.svg)](https://www.mozilla.org/en-US/MPL/2.0/)

## Description

The **dynamic-mapping-server** is a microservice of the [GridSuite](https://github.com/gridsuite) platform dedicated to **mapping network equipment to dynamic simulation models**, used as an input by dynamic simulation tools (e.g. Dynawo).

It provides the following capabilities:

- **Manage mappings**: create, read, update, delete, duplicate and export mappings, i.e. sets of rules associating network equipment (identified by property-based filters) with dynamic **models**.
- **Manage models**: create, read and delete dynamic models along with their **parameter definitions**, **variable definitions**, **variable sets** and **parameter sets** (grouped by parameter set groups).
- **Manage parameters**: export parameter sets used by the models of a mapping into the `*.par` format expected by dynamic simulation tools.
- **Match rules against a network**: given a network (from an existing case in an attached study), resolve rule/filter criteria to the actual equipment ids they match, and expose the available property values of the network.
- Expose **automaton definitions** used to describe dynamic automata (e.g. protections, controllers) associated with models.

A **rule** is the core building block of a mapping. It answers the question: *"for equipment of a given type matching a given filter, which dynamic model and parameter set should be applied?"* Each rule combines:
- an **equipment type** (e.g. generator, line, load),
- a **filter** (an expert filter, delegated to the [filter-server](https://github.com/gridsuite/filter-server)) describing the matching criteria (e.g. country, nominal voltage, etc.),
- a **dynamic model** to associate with the matched equipment,
- a **parameter set group** providing the parameter values for that model.

This avoids mapping equipment one by one: a single rule (e.g. "all generators in France with nominal voltage 400kV") automatically applies to every matching equipment of the network. When resolving the dynamic model of a network, the server evaluates each rule's filter against the network, resolves the matching equipment ids, and associates them with the corresponding model and parameters — producing the complete dynamic model consumed by the dynamic-simulation-server.

---
## Technical Stack

- Spring Boot (Web, Data JPA, Actuator)
- PostgreSQL
- Liquibase
- API documentation : OpenAPI / Swagger (`springdoc`)
- Micrometer / Prometheus
- [powsybl-network-store-client](https://github.com/powsybl/powsybl-network-store): loads networks and reads their equipment/properties.
- [gridsuite-filter](https://github.com/gridsuite/filter): equipment filters used to describe mapping rules and resolve matching equipment.
- ANTLR StringTemplate (`ST4`): used to render `*.par` parameter files from parameter sets.

---

## Development Scripts

Build Docker image

```shell
mvn install -DskipTests -Dpowsybl.docker.install
```

Please read [liquibase usage](https://github.com/powsybl/powsybl-parent/#liquibase-usage) for instructions to automatically generate changesets. After you generated a changeset do not forget to add it to git and in src/resource/db/changelog/db.changelog-master.yml


---

## Interactions with Other Microservices

```text
┌──────────────────────────┐
│  dynamic-mapping-server  │──► network-store-server            (read network topology/properties)
│                          │──► case-server                     (retrieve cases for uploaded networks)
│                          │──► network-conversion-server       (import networks from uploaded files)
│                          │──► filter-server                   (resolve equipment filters used by mapping rules)
└──────────────────────────┘
```

---

## Domain Model

| Concept | Description |
|---|---|
| **Mapping** | A set of rules, each associating an equipment filter (property-based criteria) with a dynamic model. |
| **Model** | A dynamic simulation model, defined by its parameter definitions, variable definitions and variable sets. |
| **Automaton** | A dynamic automaton (e.g. protection, controller) definition associated with models. |
| **Parameter set / Parameter set group** | Named collections of parameter values for a model, usable across mappings. |
| **Variable set** | A named collection of model variable definitions, reusable across models. |

---

## Rest API

The REST API is exposed under the following resource groups:

| Resource | Description |
|---|---|
| `/mappings` | CRUD, duplication and JSON export of mappings, and listing of models used in a mapping. |
| `/models` | CRUD on models, parameter definitions, variable definitions, variable sets and parameter set groups. |
| `/network` | Load and inspect networks, and resolve rules to matched equipment ids. |
| `/parameters` | Export parameter sets used by a mapping's models into `*.par` format. |
| `/supervision` | Admin-only endpoints (e.g. list filter uuids referenced by the mapping server). |

Full details are available through the OpenAPI / Swagger UI once the service is running.

---

## Useful Links

You can find [information on Dynawo here](https://dynawo.github.io/), the reference dynamic simulation tool consuming the mappings and parameters produced by this server.
