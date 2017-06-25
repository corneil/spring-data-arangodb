![ArangoDB-Logo](https://www.arangodb.com/wp-content/uploads/2012/10/logo_arangodb_transp.png)

# Spring Data Arangodb

The primary goal of the [Spring Data](http://projects.spring.io/spring-data) project is to make it easier to provide repository implementations for data store technologies. 
This module deals with support for [ArangoDB](https://www.arangodb.com).

## Planned Features ##

* ~~Implement ArangoTemplate~~
* ~~Implement SimpleArangoRepository~~
* ~~Implement Repository Factory~~
* ~~Implement @Query for AQL queries with and without named parameters.~~
* Implement query from method names.
* Implement Pageable and Sort support
* XML Spring Configuration support
* Support for operations on graph, vertices and edges
* Update documentation.


## Testing

In order to execute Integration test you will need a working Docker installation.
The integration test uses Docker image named `arangodb:3.1`
Execute the following:

```bash
./mvnw verify -P -no-int-test,int-test
```
## Maven Profiles

The build adds 2 profiles on top of the inherited list from spring-data-build.

* `no-int-test` - Activated by default to prevent execution of integration tests.
* `int-test` - Enable the execution of integration tests.