package org.springframework.data.arangodb.test;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.arangodb.support.ArangoDBFactory;
import org.springframework.data.arangodb.support.ArangoDBFactoryImpl;
import org.springframework.data.arangodb.support.ArangoTemplate;

import java.net.URISyntaxException;

@Configuration
@Slf4j
public class TestConfig {

	@Bean(value = "arangoFactory")
	public ArangoDBFactory arangoDBFactory() throws URISyntaxException {
		String url = String.format("vst://%s:%s/testdb", System.getProperty("arangodb.host", "localhost"), System.getProperty("arangodb.port","8529"));
		log.info("ArangoDB:URL:{}", url);
		return new ArangoDBFactoryImpl(url);
	}
}
