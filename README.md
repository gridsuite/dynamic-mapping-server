[![MPL-2.0 License](https://img.shields.io/badge/license-MPL_2.0-blue.svg)](https://www.mozilla.org/en-US/MPL/2.0/)
# dynamic-mapping-server

to generate a new changeSet (diff between current schema & new schema ) :
```
mvn compile && mvn liquibase:dropAll@dummyDB && mvn liquibase:update@dummyDB && mvn liquibase:diff@dummyDB
```
to generate an initial changelog from existing database
```
mvn org.liquibase:liquibase-maven-plugin:generateChangeLog
```
to use an existing  database without destroying it
```
mvn liquibase:changelogSync
```
to update current data base (withou running application)
```
mvn liquibase:update
```
rollback:
```
mvn liquibase:rollback -Dliquibase.rollbackCount=1
```
history:
```
mvn liquibase:history
```
to get more information (and more options)
```
mvn liquibase:help
```

Parameters are not yet generated by the app. For now the app will not work as intended without any data.

After creating the database and its tables, load **src/main/resources/IEEE14Models.sql** to instantiate some. 