package org.springframework.data.arangodb.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.springframework.data.arangodb.test.basic.ArangoBasicITest;
import org.springframework.data.arangodb.test.query.ArangoQueryITest;
import org.springframework.data.arangodb.test.repo.ArangoRepositoryITest;

@RunWith(Suite.class)
@Suite.SuiteClasses({
	ArangoBasicITest.class,
	ArangoRepositoryITest.class,
	ArangoQueryITest.class
})
public class ArangoITCase {
}
