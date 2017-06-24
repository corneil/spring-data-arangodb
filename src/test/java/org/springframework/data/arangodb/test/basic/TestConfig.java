package org.springframework.data.arangodb.test.basic;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.arangodb.support.ArangoTemplate;

import java.net.URISyntaxException;

@Configuration
@Slf4j
public class TestConfig {
	@Bean(value = "arangoTemplate")
	@Autowired
	public ArangoTemplate arangoTemplate(Environment environment) throws URISyntaxException {
		String url = String.format("vst://%s:%s/testdb", environment.getRequiredProperty("arangodb.host"), environment.getRequiredProperty("arangodb.port"));
		log.info("ArangoDB:URL:{}", url);
		final ArangoTemplate arangoTemplate = new ArangoTemplate(url);
		arangoTemplate.waitForSync(true);
		return arangoTemplate;
	}
}
