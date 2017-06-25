package org.springframework.data.arangodb.support;

import com.arangodb.ArangoCollection;
import com.arangodb.ArangoCursor;
import com.arangodb.ArangoDB;
import com.arangodb.entity.CollectionPropertiesEntity;
import com.arangodb.entity.CursorEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.arangodb.repository.support.ArangoEntityInformation;
import org.springframework.data.arangodb.repository.support.ArangoOperations;
import org.springframework.util.Assert;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ArangoTemplate implements ArangoOperations {
	private static final Logger log = LoggerFactory.getLogger(ArangoTemplate.class);

	private Map<Class, ArangoEntityInformation> entityInformation = new ConcurrentHashMap<Class, ArangoEntityInformation>();

	private ArangoDBFactory databaseFactory;

	public ArangoTemplate(ArangoDBFactory databaseFactory) {
		log.debug("ArangoTemplate:databaseFactory");
		this.databaseFactory = databaseFactory;
	}

	public ArangoTemplate(final String databaseName, ArangoDB db) {
		log.debug("ArangoTemplate:{}", databaseName);
		this.databaseFactory = new ArangoDBFactoryImpl(databaseName, db);
	}

	@Override
	public <T> long count(Class<T> domainClass) {
		ArangoEntityInformation info = findEntityInformation(domainClass);
		CollectionPropertiesEntity propertiesEntity = databaseFactory.getCollection(info.getCollectionName()).count();
		return propertiesEntity.getCount();
	}

	@Override
	public <T> void delete(Class<T> domainClass, String id) {
		ArangoEntityInformation info = findEntityInformation(domainClass);
		databaseFactory.getCollection(info.getCollectionName()).deleteDocument(id);
	}

	@Override
	public <T> void delete(T entity) {
		ArangoEntityInformation info = findEntityInformation(entity.getClass());
		databaseFactory.getCollection(info.getCollectionName()).deleteDocument(info.getId(entity));
	}

	@Override
	public <T> void deleteAll(Class<T> domainClass) {
		ArangoEntityInformation info = findEntityInformation(domainClass);
		databaseFactory.getCollection(info.getCollectionName()).truncate();
	}

	@Override
	public <T> void deleteAll(Class<T> domainClass, Iterable<? extends T> iterable) {
		ArangoEntityInformation info = findEntityInformation(domainClass);
		Set<String> keys = new HashSet<String>();
		for (T entity : iterable) {
			keys.add(info.getId(entity));
		}
		databaseFactory.getCollection(info.getCollectionName()).deleteDocuments(keys);
	}

	@Override
	public boolean exists(Class domainClass, String id) {
		ArangoEntityInformation info = findEntityInformation(domainClass);
		return databaseFactory.getCollection(info.getCollectionName()).documentExists(id);
	}

	@Override
	public <T> Iterable<T> findAll(Class<T> domainClass) {
		ArangoEntityInformation info = entityInformation.get(domainClass);
		String query = String.format("FOR entity in `%s` return entity", info.getCollectionName());
		ArangoCursor<T> cursor = databaseFactory.getDatabase().query(query, null, null, domainClass);
		logWarnings(cursor);
		return cursor.asListRemaining();
	}

	@Override
	public <T> Iterable<T> findAll(Class<T> domainClass, Iterable<String> iterable) {
		ArangoEntityInformation info = entityInformation.get(domainClass);
		String query = String.format("FOR entity in `%s` FILTER _key IN @keys return entity", info.getCollectionName());
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("keys", iterable);
		ArangoCursor<T> cursor = databaseFactory.getDatabase().query(query, parameters, null, domainClass);
		logWarnings(cursor);
		return cursor.asListRemaining();
	}

	@Override
	public <T> T findOne(Class<T> domainClass, String id) {
		ArangoEntityInformation info = entityInformation.get(domainClass);
		return databaseFactory.getCollection(info.getCollectionName()).getDocument(id, domainClass);
	}

	@Override
	public <T> T save(T entity) {
		ArangoEntityInformation info = findEntityInformation(entity.getClass());
		Assert.notNull(info, String.format("Entity isn't registered:%s", entity.getClass().getName()));
		final ArangoCollection collection = databaseFactory.getCollection(info.getCollectionName());
		if (info.isNew(entity)) {
			collection.insertDocument(entity);
		} else {
			final String key = info.getId(entity);
			if (collection.documentExists(key)) {
				collection.replaceDocument(key, entity);
			} else {
				collection.insertDocument(entity);
			}
		}
		return entity;
	}

	@Override
	public <T> Iterable<T> save(Iterable<T> iterable) {
		ArangoEntityInformation info = null;
		Collection<T> newEntities = null;
		Collection<T> existingEntities = null;
		ArangoCollection collection = null;
		for (T entity : iterable) {
			if (info == null) {
				info = findEntityInformation(entity.getClass());
				collection = databaseFactory.getCollection(info.getCollectionName());
			}
			if (info.isNew(entity) || !collection.documentExists(info.getId(entity))) {
				if (newEntities == null) {
					newEntities = new LinkedList<T>();
				}
				newEntities.add(entity);
			} else {
				if (existingEntities == null) {
					existingEntities = new LinkedList<T>();
				}
				existingEntities.add(entity);
			}
		}
		if (newEntities != null) {
			Assert.notNull(collection, "collection not initialised");
			collection.insertDocuments(newEntities);
		}
		if (existingEntities != null) {
			Assert.notNull(collection, "collection not initialised");
			collection.replaceDocuments(existingEntities);
		}
		return iterable;
	}

	private <T> ArangoEntityInformation findEntityInformation(Class domainClass) {
		ArangoEntityInformation info = entityInformation.get(domainClass);
		if (info == null) {
			info = new ArangoEntityInformation(domainClass);
			entityInformation.put(domainClass, info);
		}
		return info;
	}

	private <T> void logWarnings(ArangoCursor<T> cursor) {
		Collection<CursorEntity.Warning> warnings = cursor.getWarnings();
		if (warnings != null) {
			for (CursorEntity.Warning warning : warnings) {
				log.warn("findAll:{}:{}", warning.getCode(), warning.getMessage());
			}
		}
	}

	public void register(Class cls) {
		register(cls, new ArangoEntityInformation(cls));
	}

	public <T> void register(Class<T> cls, ArangoEntityInformation<T> info) {
		entityInformation.put(cls, info);
	}
}
