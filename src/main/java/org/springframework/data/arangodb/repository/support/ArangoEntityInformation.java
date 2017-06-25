package org.springframework.data.arangodb.repository.support;

import com.arangodb.entity.DocumentField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.annotation.Id;
import org.springframework.data.arangodb.repository.Document;
import org.springframework.data.repository.core.support.AbstractEntityInformation;
import org.springframework.util.Assert;

import java.lang.reflect.Field;

public class ArangoEntityInformation<T> extends AbstractEntityInformation<T, String> {
	private static final Logger log = LoggerFactory.getLogger(ArangoEntityInformation.class);

	private final String collectionName;

	private final Class<T> domainClass;

	private final Field idField;

	public ArangoEntityInformation(Class<T> domainClass) {
		super(domainClass);
		this.domainClass = domainClass;
		idField = getIdField(domainClass);
		Assert.notNull(idField, "Cannot determine key field, Annotate with Id or DocumentField");
		Assert.isAssignable(String.class, idField.getType(), "Key must be String");
		collectionName = findCollectionName(domainClass);
		log.debug("ArangoDbEntityInformation:{}:CollectionName={}", domainClass.getName(), collectionName);
	}

	private static String findCollectionName(Class domainClass) {
		if (domainClass.isAnnotationPresent(Document.class)) {
			Document document = (Document) domainClass.getAnnotation(Document.class);
			if (document.value().length() > 0) {
				return document.value();
			}
		}
		return domainClass.getSimpleName();
	}

	private static Field getIdField(Class cls) {
		Field result = null;
		for (Field field : cls.getDeclaredFields()) {
			if (field.isAnnotationPresent(DocumentField.class)) {
				DocumentField documentField = field.getAnnotation(DocumentField.class);
				if (documentField != null) {
					if (DocumentField.Type.KEY.equals(documentField.value())) {
						result = field;
						break;
					}
				}
			}
		}
		if (result == null) {
			for (Field field : cls.getDeclaredFields()) {
				if (field.isAnnotationPresent(Id.class)) {
					Id idAnnotation = field.getAnnotation(Id.class);
					if (idAnnotation != null) {
						result = field;
						break;
					}
				}
			}
		}
		return result;
	}

	public String getCollectionName() {
		return collectionName;
	}

	public Class<T> getDomainClass() {
		return domainClass;
	}

	@Override
	public String getId(T t) {
		if(idField!=null) {
			try {
				idField.setAccessible(true);
				return (String) idField.get(t);
			} catch (IllegalAccessException e) {
				log.error("getId:{},{}", t.getClass().getName(), e.toString(), e);
			}
		}
		return null;
	}

	@Override
	public Class<String> getIdType() {
		return String.class;
	}

	public Field getIdField() {
		return idField;
	}

	public String getIdFieldName() {
		return idField != null ? idField.getName() : null;
	}
}
