package org.springframework.data.arangodb.repository.support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.core.support.RepositoryFactoryBeanSupport;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;
import org.springframework.util.Assert;

public class ArangoRepositoryFactoryBean<T extends Repository<S, String>, S> extends RepositoryFactoryBeanSupport<T, S, String> {
	private static final Logger log = LoggerFactory.getLogger(ArangoRepositoryFactoryBean.class);
	private ArangoOperations operations;

	public ArangoRepositoryFactoryBean(ArangoOperations operations) {
		this.operations = operations;
	}

	public void setArangoOperations(ArangoOperations operations) {
		this.operations = operations;
	}

	protected RepositoryFactorySupport getFactoryInstance(ArangoOperations operations) {
		return new ArangoRepositoryFactory(operations);
	}

	@Override
	protected RepositoryFactorySupport createRepositoryFactory() {
		Assert.notNull(operations, "operations not configured");
		RepositoryFactorySupport support = getFactoryInstance(operations);
		// TODO add query listeners
		return support;
	}
}
