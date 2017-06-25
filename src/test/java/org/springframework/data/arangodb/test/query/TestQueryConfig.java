package org.springframework.data.arangodb.test.query;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.data.arangodb.repository.config.EnableArangoRepositories;
import org.springframework.data.arangodb.support.ArangoDBFactory;
import org.springframework.data.arangodb.support.ArangoTemplate;
import org.springframework.data.arangodb.test.TestConfig;

import java.net.URISyntaxException;

@Configuration
@Slf4j
@EnableArangoRepositories(basePackages = {"org.springframework.data.arangodb.test.query"})
@Import(TestConfig.class)
public class TestQueryConfig {
	@Bean
	@Autowired
	public ArangoTemplate arangoTemplate(ArangoDBFactory dbFactory) {
		return new ArangoTemplate(dbFactory);
	}
}
