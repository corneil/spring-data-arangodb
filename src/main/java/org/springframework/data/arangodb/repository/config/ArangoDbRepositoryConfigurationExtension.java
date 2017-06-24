package org.springframework.data.arangodb.repository.config;

import org.springframework.data.arangodb.repository.support.ArangoRepositoryFactoryBean;
import org.springframework.data.repository.config.RepositoryConfigurationExtensionSupport;

public class ArangoDbRepositoryConfigurationExtension extends RepositoryConfigurationExtensionSupport {

	@Override
	public String getRepositoryFactoryClassName() {
		return ArangoRepositoryFactoryBean.class.getName();
	}

	@Override
	protected String getModulePrefix() {
		return "arangodb";
	}

}
