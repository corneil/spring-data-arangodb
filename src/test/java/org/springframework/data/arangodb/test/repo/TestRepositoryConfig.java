package org.springframework.data.arangodb.test.repo;

import lombok.extern.slf4j.XSlf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.arangodb.repository.config.EnableArangoRepositories;
import org.springframework.data.arangodb.support.ArangoDBFactory;
import org.springframework.data.arangodb.support.ArangoTemplate;
import org.springframework.data.arangodb.test.TestConfig;

@Configuration
@XSlf4j
@EnableArangoRepositories(basePackages = {"org.springframework.data.arangodb.test.repo"})
@Import(TestConfig.class)
public class TestRepositoryConfig {

	@Bean(name = "arangoTemplate")
	@Autowired
	public ArangoTemplate arangoTemplate(ArangoDBFactory dbFactory) {
		log.entry(dbFactory);
		return log.exit(new ArangoTemplate(dbFactory));
	}
}
