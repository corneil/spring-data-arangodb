package org.springframework.data.arangodb.support;

import com.arangodb.ArangoCollection;
import com.arangodb.ArangoCursor;
import com.arangodb.ArangoDB;
import com.arangodb.ArangoDBException;
import com.arangodb.ArangoDatabase;
import com.arangodb.Protocol;
import com.arangodb.entity.CollectionPropertiesEntity;
import com.arangodb.entity.CursorEntity;
import com.arangodb.entity.DatabaseEntity;
import com.arangodb.model.DocumentCreateOptions;
import com.arangodb.model.DocumentReplaceOptions;
import com.arangodb.model.DocumentUpdateOptions;
import com.arangodb.velocypack.VPackModule;
import lombok.extern.slf4j.XSlf4j;
import org.springframework.data.arangodb.repository.UncategorizedArangoException;
import org.springframework.data.arangodb.repository.support.ArangoEntityInformation;
import org.springframework.data.arangodb.repository.support.ArangoOperations;
import org.springframework.util.Assert;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

@XSlf4j
public class ArangoTemplate implements ArangoOperations {
	public static final String ARANGODB = "arangodb.";

	public static final String CHUNKSIZE = "chunksize";

	public static final String CONNECTIONS_MAX = "connections.max";

	public static final String DBNAME = "database";

	public static final String HOSTS = "hosts";

	public static final String PASSWORD = "password";

	public static final String PROTOCOL = "protocol";

	public static final String TIMEOUT = "timeout";

	public static final String USER = "user";

	public static final String USE_SSL = "useSsl";

	private DocumentReplaceOptions replaceOptions = new DocumentReplaceOptions();

	private Set<String> verifiedCollections = new ConcurrentSkipListSet<String>();

	private DocumentCreateOptions createOptions = new DocumentCreateOptions();

	private DocumentUpdateOptions updateOptions = new DocumentUpdateOptions();

	private ArangoDB arangoDB;

	private String databaseName;

	private Map<Class, ArangoEntityInformation> entityInformation = new ConcurrentHashMap<Class, ArangoEntityInformation>();

	public ArangoTemplate(Properties properties) {
		init(properties);
	}

	public ArangoTemplate(String url) throws URISyntaxException {
		init(url);
	}

	public void createDatabaseIfNotExists() {
		try {
			DatabaseEntity databaseEntity = arangoDB.db(databaseName).getInfo();
			Assert.isTrue(databaseEntity.getName().equalsIgnoreCase(databaseName), String.format("Expected:%s=%s", databaseEntity.getName(), databaseName));
		} catch (ArangoDBException x) {
			if (x.getErrorNum() == 1228) {
				arangoDB.createDatabase(databaseName);
			} else {
				throw new UncategorizedArangoException(x.toString(), x);
			}
		}
	}

	public void dropDatabaseIfExists() {
		try {
			arangoDB.db(databaseName).drop();
			verifiedCollections.clear();
		} catch (ArangoDBException x) {
			if (x.getErrorNum() != 1228) {
				throw new UncategorizedArangoException(x.toString(), x);
			}
		}
	}

	private void addHost(ArangoDB.Builder builder, String host) {
		String hostName = null;
		String port = null;
		int idx = host.indexOf(':');
		if (idx >= 0) {
			String before = host.substring(0, idx);
			String after = host.substring(idx + 1);
			if (before.length() > 0) {
				hostName = before;
			}
			if (after.length() > 0) {
				port = after;
			}
		}
		log.debug("addHost:{}={}:{}", host, hostName, port);
		if (hostName == null || port == null) {
			throw new RuntimeException("arangodb.hosts form is hostname:port");
		}
		builder.host(hostName, Integer.parseInt(port));
	}

	private void addHosts(ArangoDB.Builder builder, String hosts) {
		StringTokenizer tokenizer = new StringTokenizer(hosts, ", ");
		while (tokenizer.hasMoreTokens()) {
			addHost(builder, tokenizer.nextToken());
		}
	}

	@Override
	public <T> long count(Class<T> domainClass) {
		ArangoEntityInformation info = findEntityInformation(domainClass);
		CollectionPropertiesEntity propertiesEntity = getCollection(info.getCollectionName()).count();
		return propertiesEntity.getCount();
	}

	@Override
	public <T> void delete(Class<T> domainClass, String id) {
		ArangoEntityInformation info = findEntityInformation(domainClass);
		getCollection(info.getCollectionName()).deleteDocument(id);
	}

	@Override
	public <T> void delete(T entity) {
		ArangoEntityInformation info = findEntityInformation(entity.getClass());
		getCollection(info.getCollectionName()).deleteDocument(info.getId(entity));
	}

	@Override
	public <T> void deleteAll(Class<T> domainClass) {
		ArangoEntityInformation info = findEntityInformation(domainClass);
		getCollection(info.getCollectionName()).truncate();
	}

	@Override
	public <T> void deleteAll(Class<T> domainClass, Iterable<? extends T> iterable) {
		ArangoEntityInformation info = findEntityInformation(domainClass);
		Set<String> keys = new HashSet<String>();
		for (T entity : iterable) {
			keys.add(info.getId(entity));
		}
		getCollection(info.getCollectionName()).deleteDocuments(keys);
	}

	@Override
	public boolean exists(Class domainClass, String id) {
		ArangoEntityInformation info = findEntityInformation(domainClass);
		return getCollection(info.getCollectionName()).documentExists(id);
	}

	@Override
	public <T> Iterable<T> findAll(Class<T> domainClass) {
		ArangoEntityInformation info = entityInformation.get(domainClass);
		String query = String.format("FOR entity in `%s` return entity", info.getCollectionName());
		ArangoCursor<T> cursor = arangoDB.db(databaseName).query(query, null, null, domainClass);
		logWarnings(cursor);
		return cursor.asListRemaining();
	}

	@Override
	public <T> Iterable<T> findAll(Class<T> domainClass, Iterable<String> iterable) {
		ArangoEntityInformation info = entityInformation.get(domainClass);
		String query = String.format("FOR entity in `%s` FILTER _key IN @keys return entity", info.getCollectionName());
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("keys", iterable);
		ArangoCursor<T> cursor = arangoDB.db(databaseName).query(query, parameters, null, domainClass);
		logWarnings(cursor);
		return cursor.asListRemaining();
	}

	@Override
	public <T> T findOne(Class<T> domainClass, String id) {
		ArangoEntityInformation info = entityInformation.get(domainClass);
		return getCollection(info.getCollectionName()).getDocument(id, domainClass);
	}

	@Override
	public <T> T save(T entity) {
		ArangoEntityInformation info = findEntityInformation(entity.getClass());
		Assert.notNull(info, String.format("Entity isn't registered:%s", entity.getClass().getName()));
		final ArangoCollection collection = getCollection(info.getCollectionName());
		if (info.isNew(entity)) {
			collection.insertDocument(entity, createOptions);
		} else {
			final String key = info.getId(entity);
			if (collection.documentExists(key)) {
				collection.replaceDocument(key, entity, replaceOptions);
			} else {
				collection.insertDocument(entity, createOptions);
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
				collection = getCollection(info.getCollectionName());
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
			collection.insertDocuments(newEntities, createOptions);
		}
		if (existingEntities != null) {
			Assert.notNull(collection, "collection not initialised");
			collection.replaceDocuments(existingEntities, replaceOptions);
		}
		return iterable;
	}

	private String expand(final String name) {
		if (name.startsWith(ARANGODB)) {
			return name;
		}
		StringBuilder builder = new StringBuilder();
		builder.append(ARANGODB);
		builder.append(name);
		return builder.toString();
	}

	private <T> ArangoEntityInformation findEntityInformation(Class domainClass) {
		ArangoEntityInformation info = entityInformation.get(domainClass);
		if (info == null) {
			info = new ArangoEntityInformation(domainClass);
			entityInformation.put(domainClass, info);
		}
		return info;
	}

	public ArangoDB getArangoDB() {
		return arangoDB;
	}

	public void setArangoDB(ArangoDB arangoDB) {
		this.arangoDB = arangoDB;
	}

	private Boolean getBoolean(String name, String value) {
		if (value != null) {
			return Boolean.parseBoolean(value);
		}
		return null;
	}

	private ArangoCollection getCollection(final String collectionName) {
		ArangoDatabase database = arangoDB.db(databaseName);
		if (!verifiedCollections.contains(collectionName)) {
			try {
				database.createCollection(collectionName);
			} catch (ArangoDBException x) {
				log.error("getCollection:{}", x.toString(), x);
			}
			verifiedCollections.add(collectionName);
		}
		return database.collection(collectionName);
	}

	private Integer getInteger(String value, String name) {
		if (value != null) {
			try {
				return Integer.parseInt(value);
			} catch (NumberFormatException x) {
				throw new RuntimeException(String.format("Invalid integer in %s:%s", name, value));
			}
		}
		return null;
	}

	/**
	 * URL can be:
	 * http://127.0.0.1:8529/database?connections=value&chuncksize=value&useSsl=true|false&timeout=value&user=value&password=value
	 *
	 * @param url
	 */
	public void init(String url) throws URISyntaxException {
		URI uri = new URI(url);
		ArangoDB.Builder builder = new ArangoDB.Builder();
		String protocolStr = uri.getScheme();
		setProtocol(builder, protocolStr);
		addHosts(builder, uri.getAuthority());
		setDatabaseName(uri.getPath());
		String query = uri.getQuery();
		if (query != null) {
			StringTokenizer tokenizer = new StringTokenizer(query, "&");
			while (tokenizer.hasMoreTokens()) {
				String token = tokenizer.nextToken();
				int idx = token.indexOf("=");
				String name = token.substring(0, idx);
				String value = token.substring(idx + 1);
				setParameter(builder, name, value);
			}
		}
		checkJava8Date(builder);
		checkJodaDate(builder);
		arangoDB = builder.build();
	}

	private void checkJava8Date(ArangoDB.Builder builder) {
		try {
			Class cls = Class.forName("com.arangodb.velocypack.module.jdk8.VPackJdk8Module");
			builder.registerModule((VPackModule) cls.newInstance());
		} catch (Throwable x) {
			// ignore
		}
	}

	private void checkJodaDate(ArangoDB.Builder builder) {
		try {
			Class cls = Class.forName("com.arangodb.velocypack.module.joda.VPackJodaModule");
			builder.registerModule((VPackModule) cls.newInstance());
		} catch (Throwable x) {
			// ignore
		}
	}

	public void init(final Properties properties) {
		final ArangoDB.Builder builder = new ArangoDB.Builder();
		setDatabaseName(properties.getProperty(expand(DBNAME)));
		final String hosts = properties.getProperty(expand(HOSTS));
		if (hosts != null) {
			addHosts(builder, hosts);
		}
		final List<String> options = Arrays.asList(CONNECTIONS_MAX, CHUNKSIZE, USE_SSL, USER, PASSWORD, TIMEOUT);
		for (String option : options) {
			setParameter(builder, option, properties.getProperty(expand(option)));
		}
		final String protocolStr = properties.getProperty(expand(PROTOCOL));
		if (protocolStr != null) {
			setProtocol(builder, protocolStr);
		}
		checkJava8Date(builder);
		checkJodaDate(builder);
		arangoDB = builder.build();
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

	private void setDatabaseName(String name) {
		this.databaseName = name;
		if (databaseName == null || databaseName.equalsIgnoreCase("/")) {
			throw new RuntimeException("database is required");
		}
		if (databaseName.startsWith("/")) {
			databaseName = databaseName.substring(1);
		}
		StringBuilder stringBuilder = new StringBuilder();
		for (int i = 0; i < databaseName.length(); i++) {
			char c = databaseName.charAt(i);
			if (!Character.isLetterOrDigit(c)) {
				stringBuilder.append('_');
			} else {
				stringBuilder.append(c);
			}
		}
		databaseName = stringBuilder.toString();
	}

	private void setParameter(final ArangoDB.Builder builder, final String name, final String value) {
		if (value != null) {
			log.debug("setParameter:{}={}", name, value);
			if (USER.equalsIgnoreCase(name)) {
				builder.user(value);
			} else if (PASSWORD.equalsIgnoreCase(name)) {
				builder.password(value);
			} else if (CHUNKSIZE.equalsIgnoreCase(name)) {
				builder.chunksize(getInteger(name, value));
			} else if (TIMEOUT.equalsIgnoreCase(name)) {
				builder.timeout(getInteger(name, value));
			} else if (USE_SSL.equalsIgnoreCase(name)) {
				builder.useSsl(getBoolean(name, value));
			} else if (CONNECTIONS_MAX.equalsIgnoreCase(name)) {
				builder.maxConnections(getInteger(name, value));
			} else {
				throw new RuntimeException(String.format("Don't know how to set:%s=%s", name, value));
			}
		}
	}

	private void setProtocol(final ArangoDB.Builder builder, final String protocolStr) {
		Protocol protocol = null;
		for (Protocol p : Protocol.values()) {
			if (p.name().equalsIgnoreCase(protocolStr)) {
				protocol = p;
			}
		}
		if (protocol == null) {
			throw new RuntimeException(String.format("protocol must be one of %s not %s", Arrays.asList(Protocol.values()), protocolStr));
		}
		builder.useProtocol(protocol);
	}

	public void waitForSync(boolean wait) {
		createOptions.waitForSync(wait);
		updateOptions.waitForSync(wait);
		replaceOptions.waitForSync(wait);
	}
}
