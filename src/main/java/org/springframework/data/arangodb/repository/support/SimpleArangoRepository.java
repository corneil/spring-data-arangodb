package org.springframework.data.arangodb.repository.support;

import org.springframework.data.arangodb.repository.ArangoRepository;
import org.springframework.stereotype.Repository;

@Repository
public class SimpleArangoRepository<T> implements ArangoRepository<T> {
	private ArangoOperations template;

	private ArangoEntityInformation<T> entityInformation;

	public SimpleArangoRepository(ArangoEntityInformation<T> entityInformation, ArangoOperations template) {
		this.template = template;
		this.entityInformation = entityInformation;
	}

	@Override
	public <S extends T> S save(S s) {
		return template.save(s);
	}

	@Override
	public <S extends T> Iterable<S> save(Iterable<S> iterable) {
		return template.save(iterable);
	}

	@Override
	public T findOne(String id) {
		return template.findOne(entityInformation.getDomainClass(), id);
	}

	@Override
	public boolean exists(String id) {
		return template.exists(entityInformation.getDomainClass(), id);
	}

	@Override
	public Iterable<T> findAll() {
		return template.findAll(entityInformation.getDomainClass());
	}

	@Override
	public Iterable<T> findAll(Iterable<String> iterable) {
		return template.findAll(entityInformation.getDomainClass(), iterable);
	}

	@Override
	public long count() {
		return template.count(entityInformation.getDomainClass());
	}

	@Override
	public void delete(String id) {
		template.delete(entityInformation.getDomainClass(), id);
	}

	@Override
	public void delete(T t) {
		template.delete(t);
	}

	@Override
	public void delete(Iterable<? extends T> iterable) {
		template.deleteAll(entityInformation.getDomainClass(), iterable);
	}

	@Override
	public void deleteAll() {
		template.deleteAll(entityInformation.getDomainClass());
	}
}
