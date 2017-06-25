package org.springframework.data.arangodb.repository.support;

import java.util.Collection;
import java.util.Map;

public interface ArangoOperations {
	<T> long count(Class<T> domainClass);
	<T> void delete(Class<T> domainClass, String id);
	<T> void delete(T entity);
	<T> void deleteAll(Class<T> domainClass);
	<T> void deleteAll(Class<T> domainClass, Iterable<? extends T> iterable);
	boolean exists(Class domainClass, String id);
	<T> Iterable<T> findAll(Class<T> domainClass);
	<T> Iterable<T> findAll(Class<T> domainClass, Iterable<String> iterable);
	<T> T findOne(Class<T> domainClass, String id);
	<T> T save(T entity);
	<T> Iterable<T> save(Iterable<T> iterable);
	<T> Iterable<T> query(Class<T> domainClass, String queryString, Map<String,Object> parameters);
}
