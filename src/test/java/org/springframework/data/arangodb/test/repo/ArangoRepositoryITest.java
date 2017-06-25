package org.springframework.data.arangodb.test.repo;

import org.hamcrest.Matchers;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.arangodb.support.ArangoDBFactory;
import org.springframework.data.arangodb.test.TestConfig;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import java.util.LinkedList;
import java.util.List;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class, classes = {TestConfig.class, TestRepositoryConfig.class})
public class ArangoRepositoryITest {
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
	public void createUser() {
		User user = new User("johndoe", "John Doe", LocalDate.parse("1991-11-12"));
		userRepository.save(user);
		User loaded = userRepository.findOne("johndoe");
		assertThat(loaded, is(notNullValue()));
		assertThat(loaded.getDateOfBirth(), is(equalTo(user.getDateOfBirth())));
		assertThat(loaded.getFullName(), is(equalTo(user.getFullName())));
		assertThat(loaded.getUserId(), is(equalTo(user.getUserId())));
		loaded.setFullName("John Doe I");
		userRepository.save(loaded);
		user = userRepository.findOne("johndoe");
		assertThat(user, is(notNullValue()));
		assertThat(user.getFullName(), is(equalTo(loaded.getFullName())));
	}

	@Test
	public void createListUsers() {
		List<User> users = new LinkedList<User>();
		users.add(new User("johndoe", "John Doe", LocalDate.parse("1991-10-11")));
		users.add(new User("janedoe", "Jane Doe", LocalDate.parse("1991-11-12")));
		userRepository.save(users);
		assertThat(userRepository.count(), is(2L));
		User johndoe = userRepository.findOne("johndoe");
		assertThat(johndoe, is(notNullValue()));
		assertThat(johndoe.getFullName(), is(equalTo("John Doe")));
		List<User> loaded = new LinkedList<User>();
		for (User user : userRepository.findAll()) {
			loaded.add(user);
		}
		assertThat(loaded, is(not(Matchers.<User>empty())));
		assertThat(loaded.size(), is(2));
	}

	@Test
	public void deleteUsers() {
		List<User> users = new LinkedList<User>();
		users.add(new User("johndoe", "John Doe", LocalDate.parse("1991-10-11")));
		final User janedoe = new User("janedoe", "Jane Doe", LocalDate.parse("1991-11-12"));
		users.add(janedoe);
		userRepository.save(users);
		assertThat(userRepository.count(), is(2L));
		userRepository.delete("johndoe");
		assertThat(userRepository.count(), is(1L));
		userRepository.delete(janedoe);
		assertThat(userRepository.count(), is(0L));
		userRepository.save(users);
		assertThat(userRepository.count(), is(2L));
		userRepository.deleteAll();
		assertThat(userRepository.count(), is(0L));
	}
}
