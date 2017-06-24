package org.springframework.data.arangodb.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.PagingAndSortingRepository;

@NoRepositoryBean
public interface ArangoRepository<T> extends CrudRepository<T, String> {
}
