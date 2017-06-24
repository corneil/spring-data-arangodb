package org.springframework.data.arangodb.repository.support;

import org.springframework.data.repository.Repository;
import org.springframework.data.repository.core.support.RepositoryFactoryBeanSupport;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;

public class ArangoRepositoryFactoryBean<T extends Repository<S, String>, S> extends RepositoryFactoryBeanSupport<T, S, String> {
	private ArangoOperations operations;

	public ArangoRepositoryFactoryBean(Class<? extends T> repositoryInterface, ArangoOperations operations) {
		super(repositoryInterface);
		this.operations = operations;
	}

	@Override
	protected RepositoryFactorySupport createRepositoryFactory() {
		return new ArangoRepositoryFactory(operations);
	}
}
