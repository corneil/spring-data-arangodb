package org.springframework.data.arangodb.repository.query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.arangodb.repository.support.ArangoEntityInformation;
import org.springframework.data.arangodb.repository.support.ArangoOperations;
import org.springframework.data.repository.query.QueryMethod;
import org.springframework.util.Assert;

import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ArangoAQLQuery extends AbstractArangoQuery {
	private static final Logger LOGGER = LoggerFactory.getLogger(ArangoAQLQuery.class);

	private final ArangoOperations operations;

	private final ArangoEntityInformation information;

	private final Class<?> domainClass;

	private final ArangoQueryMethod method;

	private final String queryString;

	private final ArangoParameters parameters;

	public ArangoAQLQuery(Class<?> domainClass, ArangoQueryMethod method, ArangoOperations operations) {
		this.method = method;
		this.domainClass = domainClass;
		this.operations = operations;
		this.parameters = method.getParameters();
		this.queryString = method.getQueryAnnotationString();
		this.information = new ArangoEntityInformation(domainClass);
		Assert.isTrue(method.hasQueryAnnotatation(), "Expected method.hasQueryAnnotation() == true");
	}

	@Override
	public Object execute(Object[] objects) {
		Map<String, Object> bind = new HashMap<String, Object>();
		String queryString = method.getQueryAnnotationString();
		if (objects != null) {
			ArangoParameters bindable = parameters.getBindableParameters();
			for (int i = 0; i < objects.length; i++) {
				Assert.isTrue(bindable.hasParameterAt(i), String.format("Expected bindable parameter at %d", i));
				ArangoParameters.ArangoParameter parameter = bindable.getParameter(i);
				if (!parameter.isExplicitlyNamed()) {
					String name = String.format("param_%d", i);
					queryString = queryString.replaceFirst("\\?", String.format("@%s", name));
					bind.put(name, objects[i]);
				} else {
					bind.put(parameter.getName(), objects[i]);
				}
			}
		}
		if (information.getIdField() != null) {
			queryString = queryString.replace(String.format(".%s", information.getIdFieldName()), "._key");
		}
		LOGGER.debug("execute:queryString:{}", queryString);
		LOGGER.debug("execute:parameters:{}", bind);
		Iterable result = operations.query(domainClass, queryString, bind);
		if (Set.class.isAssignableFrom(method.getReturnType())) {
			if (result instanceof Set) {
				return result;
			} else {
				Set set = new HashSet();
				for (Object entity : result) {
					set.add(entity);
				}
				return set;
			}
		} else if (List.class.isAssignableFrom(method.getReturnType())) {
			if (result instanceof List) {
				return result;
			} else {
				List list = new LinkedList();
				for (Object entity : result) {
					list.add(entity);
				}
				return list;
			}
		} else if (Iterable.class.isAssignableFrom(method.getReturnType())) {
			return result;
		} else if (method.getReturnType().isArray()) {
			List list = new LinkedList();
			for (Object entity : result) {
				list.add(entity);
			}
			return list.toArray((Object[]) Array.newInstance(method.getReturnType(), list.size()));
		}
		// Assuming return type is object.
		Object obj = null;
		for (Object entity : result) {
			if (obj == null) {
				obj = entity;
			} else {
				LOGGER.warn("Results contained more than one result:{}", entity);
			}
		}
		return obj;
	}

	@Override
	public QueryMethod getQueryMethod() {
		return method;
	}
}
