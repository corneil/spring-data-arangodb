package org.springframework.data.arangodb.repository.query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.arangodb.repository.support.ArangoOperations;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.repository.core.NamedQueries;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.query.EvaluationContextProvider;
import org.springframework.data.repository.query.QueryLookupStrategy;
import org.springframework.data.repository.query.RepositoryQuery;

import java.lang.reflect.Method;

public class ArangoQueryLookupStrategy {
	private static final Logger LOGGER = LoggerFactory.getLogger(ArangoQueryLookupStrategy.class);

	private abstract static class AbstractQueryLookupStrategy implements QueryLookupStrategy {
		protected ArangoOperations operations;

		public AbstractQueryLookupStrategy(ArangoOperations operations) {
			this.operations = operations;
		}

		@Override
		public RepositoryQuery resolveQuery(Method method,
											RepositoryMetadata repositoryMetadata,
											ProjectionFactory projectionFactory,
											NamedQueries namedQueries) {
			LOGGER.debug("resolveQuery:{}:{}", method, repositoryMetadata.getDomainType());
			return resolveQuery(new ArangoQueryMethod(method, repositoryMetadata, projectionFactory), repositoryMetadata, namedQueries, operations);
		}

		protected abstract RepositoryQuery resolveQuery(ArangoQueryMethod method,
														RepositoryMetadata repositoryMetadata,
														NamedQueries namedQueries,
														ArangoOperations operation);
	}

	private static class CreateQueryLookupStrategy extends AbstractQueryLookupStrategy {
		public CreateQueryLookupStrategy(ArangoOperations operations) {
			super(operations);
		}

		@Override
		protected RepositoryQuery resolveQuery(ArangoQueryMethod method,
											   RepositoryMetadata repositoryMetadata,
											   NamedQueries namedQueries,
											   ArangoOperations operations) {
			try {
				return new PartTreeArangoQuery(repositoryMetadata.getDomainType(), method, operations);
			} catch (IllegalArgumentException e) {
				throw new IllegalArgumentException(String.format("Could not create query metamodel for method %s!", method.toString()), e);
			}
		}
	}

	private static class DeclaredQueryLookupStrategy extends AbstractQueryLookupStrategy {
		public DeclaredQueryLookupStrategy(ArangoOperations operations) {
			super(operations);
		}

		@Override
		protected RepositoryQuery resolveQuery(ArangoQueryMethod method,
											   RepositoryMetadata repositoryMetadata,
											   NamedQueries namedQueries,
											   ArangoOperations operations) {
			return new ArangoAQLQuery(repositoryMetadata.getDomainType(), method, operations);
		}
	}

	private static class CreateIfNotFoundQueryLookupStrategy extends AbstractQueryLookupStrategy {
		private final DeclaredQueryLookupStrategy lookupStrategy;

		private final CreateQueryLookupStrategy createStrategy;

		public CreateIfNotFoundQueryLookupStrategy(ArangoOperations operations,
												   CreateQueryLookupStrategy createStrategy,
												   DeclaredQueryLookupStrategy lookupStrategy) {
			super(operations);
			this.createStrategy = createStrategy;
			this.lookupStrategy = lookupStrategy;
		}

		@Override
		protected RepositoryQuery resolveQuery(ArangoQueryMethod method,
											   RepositoryMetadata repositoryMetadata,
											   NamedQueries namedQueries,
											   ArangoOperations operation) {
			try {
				return lookupStrategy.resolveQuery(method, repositoryMetadata, namedQueries, operation);
			} catch (IllegalStateException e) {
				return createStrategy.resolveQuery(method, repositoryMetadata, namedQueries, operation);
			}
		}
	}

	public static QueryLookupStrategy create(ArangoOperations operations, QueryLookupStrategy.Key key, EvaluationContextProvider evaluationContextProvider) {
		switch (key != null ? key : QueryLookupStrategy.Key.CREATE_IF_NOT_FOUND) {
			case CREATE:
				return new CreateQueryLookupStrategy(operations);
			case USE_DECLARED_QUERY:
				return new DeclaredQueryLookupStrategy(operations);
			case CREATE_IF_NOT_FOUND:
				return new CreateIfNotFoundQueryLookupStrategy(operations,
						new CreateQueryLookupStrategy(operations),
						new DeclaredQueryLookupStrategy(operations));
			default:
				throw new IllegalArgumentException(String.format("Unsupported query lookup strategy %s!", key));
		}
	}
}
