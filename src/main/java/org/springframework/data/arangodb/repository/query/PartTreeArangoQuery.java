package org.springframework.data.arangodb.repository.query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.arangodb.repository.support.ArangoOperations;
import org.springframework.data.repository.query.QueryMethod;
import org.springframework.data.repository.query.parser.PartTree;

import java.util.Arrays;

public class PartTreeArangoQuery extends AbstractArangoQuery {
	private static final Logger LOGGER = LoggerFactory.getLogger(PartTreeArangoQuery.class);

	private final Class<?> domainClass;

	private final ArangoOperations operations;

	private final ArangoQueryMethod method;

	private final ArangoParameters parameters;

	private final PartTree tree;

	public PartTreeArangoQuery(Class<?> domainClass, ArangoQueryMethod method, ArangoOperations operations) {
		this.domainClass = domainClass;
		this.method = method;
		this.tree = new PartTree(method.getName(), domainClass);
		this.operations = operations;
		this.parameters = method.getParameters();
	}

	@Override
	public Object execute(Object[] objects) {
		// TODO
		LOGGER.debug("execute:{}:{}", method.getName(), objects != null ? Arrays.asList(objects) : null);
		return null;
	}

	@Override
	public QueryMethod getQueryMethod() {
		return method;
	}
}
