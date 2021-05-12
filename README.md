[![MPL-2.0 License](https://img.shields.io/badge/license-MPL_2.0-blue.svg)](https://www.mozilla.org/en-US/MPL/2.0/)
# dynamic-mapping-server

To automatically generate the sql schema file you can use the following command:

    mvn package -DskipTests && rm src/main/resources/mappings.sql && java -jar target/gridsuite-mapping-server-1.0.0-SNAPSHOT-exec.jar --spring.jpa.properties.javax.persistence.schema-generation.scripts.action=create 
