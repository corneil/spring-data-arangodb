package org.springframework.data.arangodb.repository.config;

import java.lang.annotation.Annotation;

import org.springframework.data.repository.config.RepositoryBeanDefinitionRegistrarSupport;
import org.springframework.data.repository.config.RepositoryConfigurationExtension;

public class ArangoRepositoriesRegistar extends RepositoryBeanDefinitionRegistrarSupport {

	@Override
	protected Class<? extends Annotation> getAnnotation() {
		return EnableArangoRepositories.class;
	}

	@Override
	protected RepositoryConfigurationExtension getExtension() {
		return new ArangoDbRepositoryConfigurationExtension();
	}
}
