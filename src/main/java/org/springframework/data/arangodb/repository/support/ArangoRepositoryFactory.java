package org.springframework.data.arangodb.repository.support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.arangodb.repository.ArangoRepository;
import org.springframework.data.repository.core.EntityInformation;
import org.springframework.data.repository.core.RepositoryInformation;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;

import java.io.Serializable;

public class ArangoRepositoryFactory extends RepositoryFactorySupport {
	private static final Logger log = LoggerFactory.getLogger(ArangoRepositoryFactory.class);

	private ArangoOperations arangoOperations;

	public ArangoRepositoryFactory(ArangoOperations arangoOperations) {
		this.arangoOperations = arangoOperations;
	}

	@Override
	public <T, ID extends Serializable> EntityInformation<T, ID> getEntityInformation(Class<T> domainClass) {
		log.debug("getEntityInformation:{}", domainClass);
		return new ArangoEntityInformation(domainClass);
	}

	@Override
	protected Object getTargetRepository(RepositoryInformation information) {
		log.debug("getTargetRepository:{}:{}", information.getDomainType(), information.getIdType());
		ArangoEntityInformation entityInformation = (ArangoEntityInformation) getEntityInformation(information.getDomainType());
		return getTargetRepository(information, entityInformation);
	}

	protected ArangoRepository getTargetRepository(RepositoryInformation information, ArangoEntityInformation entityInformation) {
		return getTargetRepositoryViaReflection(information, entityInformation, arangoOperations);
	}

	@Override
	protected Class<?> getRepositoryBaseClass(RepositoryMetadata metadata) {
		return SimpleArangoRepository.class;
	}
}
