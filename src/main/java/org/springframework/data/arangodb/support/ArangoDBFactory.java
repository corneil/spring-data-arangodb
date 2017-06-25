package org.springframework.data.arangodb.support;

import com.arangodb.ArangoCollection;
import com.arangodb.ArangoDatabase;

public interface ArangoDBFactory {
	void createDatabaseIfNotExists();
	void dropDatabaseIfExists();
	ArangoCollection getCollection(final String collectionName);
	ArangoCollection getCollection(final String databaseName, final String collectionName);
	ArangoDatabase getDatabase();
	ArangoDatabase getDatabase(final String databaseName);
}
