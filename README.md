# Dynamic mapping server

[![Actions Status](https://github.com/gridsuite/dynamic-mapping-server/workflows/CI/badge.svg)](https://github.com/gridsuite/dynamic-mapping-server/actions)
[![Coverage Status](https://sonarcloud.io/api/project_badges/measure?project=org.gridsuite%3Adynamic-mapping-server&metric=coverage)](https://sonarcloud.io/component_measures?id=org.gridsuite%3Adynamic-mapping-server&metric=coverage)
[![MPL-2.0 License](https://img.shields.io/badge/license-MPL_2.0-blue.svg)](https://www.mozilla.org/en-US/MPL/2.0/)

To automatically generate the sql schema file you can use the following command:

Please read [liquibase usage](https://github.com/powsybl/powsybl-parent/#liquibase-usage) for instructions to automatically generate changesets.
After you generated a changeset do not forget to add it to git and in src/resource/db/changelog/db.changelog-master.yml


Parameters are not yet generated by the app. For now the app will not work as intended without any data.

After creating the database and its tables, load **src/main/resources/IEEE14Models.sql** to instantiate some. 

The old way to automatically generate the sql schema file (directly using hibernate) can still be used for debugging. Use the following command:
```
mvn package -DskipTests && rm src/main/resources/mappings.sql && java -jar target/gridsuite-mapping-server-1.0.0-SNAPSHOT-exec.jar --spring.jpa.properties.jakarta.persistence.schema-generation.scripts.action=create 
```
