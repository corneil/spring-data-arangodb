package org.springframework.data.arangodb.test.query;

import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.arangodb.support.ArangoDBFactory;
import org.springframework.data.arangodb.support.ArangoTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import java.util.LinkedList;
import java.util.List;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class, classes = TestQueryConfig.class)
public class ArangoQueryIT {
	@Autowired
	protected ArangoDBFactory dbFactory;

	@Autowired
	protected UserRepository userRepository;

	@Before
	public void init() {
		dbFactory.dropDatabaseIfExists();
		dbFactory.createDatabaseIfNotExists();
	}

	@Test
	public void createListUsers() {
		List<User> users = new LinkedList<User>();
		users.add(new User("johndoe", "John Doe", LocalDate.parse("1991-10-11"), true));
		users.add(new User("janedoe", "Jane Doe", LocalDate.parse("1991-11-12"),true));
		userRepository.save(users);
		assertThat(userRepository.count(), is(2L));
		List<User> loaded = new LinkedList<User>();
		for (User user : userRepository.findForUserIdLike("%doe")) {
			loaded.add(user);
		}
		assertThat(loaded.size(), is(2));
		loaded.clear();
		for (User user : userRepository.findForUserIdLikeNamed("%doe")) {
			loaded.add(user);
		}
		assertThat(loaded.size(), is(2));
		User user = userRepository.findByUserId("johndoe");
		assertThat(user, is(notNullValue()));
		assertThat(user.getFullName(), is(equalTo("John Doe")));
		user = userRepository.findByUserId("janedoe");
		assertThat(user, is(notNullValue()));
		assertThat(user.getFullName(), is(equalTo("Jane Doe")));
		user = userRepository.findByUserId("123");
		assertThat(user, is(nullValue()));
	}
}
