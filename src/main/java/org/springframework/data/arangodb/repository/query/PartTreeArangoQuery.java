package org.springframework.data.arangodb.repository.query;

import org.springframework.data.repository.query.QueryMethod;
import org.springframework.data.repository.query.parser.PartTree;

public class PartTreeArangoQuery extends AbstractArangoQuery {
	private final Class<?> domainClass;

	/** The tree. */
	private final PartTree tree;

	public PartTreeArangoQuery(Class<?> domainClass, PartTree tree) {
		this.domainClass = domainClass;
		this.tree = tree;
	}

	@Override
	public Object execute(Object[] objects) {
		return null;
	}

	@Override
	public QueryMethod getQueryMethod() {
		return null;
	}
}
