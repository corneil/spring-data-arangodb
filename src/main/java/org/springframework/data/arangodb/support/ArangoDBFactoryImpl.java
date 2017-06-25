package org.springframework.data.arangodb.support;

import com.arangodb.ArangoCollection;
import com.arangodb.ArangoDB;
import com.arangodb.ArangoDBException;
import com.arangodb.ArangoDatabase;
import com.arangodb.Protocol;
import com.arangodb.entity.DatabaseEntity;
import com.arangodb.velocypack.VPackModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.arangodb.repository.UncategorizedArangoException;
import org.springframework.util.Assert;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentSkipListSet;

public class ArangoDBFactoryImpl implements ArangoDBFactory {
	private static final Logger log = LoggerFactory.getLogger(ArangoDBFactoryImpl.class);

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

	private ArangoDB arangoDB;

	private String databaseName;

	private Set<String> verifiedCollections = new ConcurrentSkipListSet<String>();

	public ArangoDBFactoryImpl(final String databaseName, final ArangoDB db) {
		this.databaseName = databaseName;
		this.arangoDB = db;
		log.debug("ArangoDBFactoryImpl:{}", databaseName);
	}

	public ArangoDBFactoryImpl(Properties properties) {
		log.debug("ArangoDBFactoryImpl:{}", properties);
		init(properties);
	}

	public ArangoDBFactoryImpl(String url) throws URISyntaxException {
		log.debug("ArangoDBFactoryImpl:{}", url);
		URI uri = new URI(url);
		init(uri);
	}

	public ArangoDBFactoryImpl(URI uri) {
		log.debug("ArangoDBFactoryImpl:{}", uri);
		init(uri);
	}

	protected ArangoCollection getCollection(ArangoDatabase database, final String collectionName) {
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

	@Override
	public ArangoCollection getCollection(String databaseName, String collectionName) {
		return getCollection(getDatabase(databaseName), collectionName);
	}

	@Override
	public ArangoCollection getCollection(String collectionName) {
		return getCollection(getDatabase(), collectionName);
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

	private String expand(final String name) {
		if (name.startsWith(ARANGODB)) {
			return name;
		}
		StringBuilder builder = new StringBuilder();
		builder.append(ARANGODB);
		builder.append(name);
		return builder.toString();
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
	public void init(URI uri) {
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
		log.debug("setDatabaseName:{}:{}", name, databaseName);
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

	@Override
	public ArangoDatabase getDatabase() {
		Assert.notNull(arangoDB, "arangoDB not configured");
		return arangoDB.db(this.databaseName);
	}

	@Override
	public ArangoDatabase getDatabase(String databaseName) {
		return arangoDB.db(databaseName);
	}
}
