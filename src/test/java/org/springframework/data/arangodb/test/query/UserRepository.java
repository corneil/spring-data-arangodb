package org.springframework.data.arangodb.test.query;

import org.springframework.data.arangodb.repository.ArangoRepository;
import org.springframework.data.arangodb.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends ArangoRepository<User> {
	@Query("FOR u in User FILTER userId LIKE @userId RETURN u")
	Iterable<User> findForUserIdLikeNamed(@Param("userId") String userId);
	@Query("FOR u in User FILTER userId LIKE ? RETURN u")
	Iterable<User> findForUserIdLike(String userId);
}
