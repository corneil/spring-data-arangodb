package org.springframework.data.arangodb.repository.query;

import org.springframework.data.arangodb.repository.Query;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.query.QueryMethod;

import java.lang.reflect.Method;

public class ArangoQueryMethod extends QueryMethod {
	private final Method method;

	public ArangoQueryMethod(Method method, RepositoryMetadata metadata, ProjectionFactory factory) {
		super(method, metadata, factory);
		this.method = method;
	}

	public Method getMethod() {
		return method;
	}

	public boolean hasQueryAnnotatation() {
		return method.isAnnotationPresent(Query.class);
	}

	public String getQueryAnnotationString() {
		Query query = getQueryAnnotation();
		return query != null ? query.value() : null;
	}

	Class<?> getReturnType() {

		return method.getReturnType();
	}
	public Query getQueryAnnotation() {
		return method.getAnnotation(Query.class);
	}

	@Override
	public ArangoParameters getParameters() {
		return (ArangoParameters)super.getParameters();
	}

	@Override
	protected ArangoParameters createParameters(Method method) {
		return new ArangoParameters(method);
	}
}
