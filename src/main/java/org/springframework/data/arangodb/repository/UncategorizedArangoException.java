package org.springframework.data.arangodb.repository;

import org.springframework.dao.UncategorizedDataAccessException;

public class UncategorizedArangoException extends UncategorizedDataAccessException {
	public UncategorizedArangoException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
