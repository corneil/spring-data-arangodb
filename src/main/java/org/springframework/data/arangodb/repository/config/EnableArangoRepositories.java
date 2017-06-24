package org.springframework.data.arangodb.repository.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.data.arangodb.repository.support.ArangoRepositoryFactoryBean;
import org.springframework.data.repository.config.DefaultRepositoryBaseClass;
import org.springframework.data.repository.query.QueryLookupStrategy;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Import(ArangoRepositoriesRegistar.class)
public @interface EnableArangoRepositories {
	String[] value() default {};
	String[] basePackages() default {};
	Class<?>[] basePackageClasses() default {};
	ComponentScan.Filter[] includeFilters() default {};
	ComponentScan.Filter[] excludeFilters() default {};
	String repositoryImplementationPostfix() default "Impl";
	Class<?> repositoryFactoryBeanClass() default ArangoRepositoryFactoryBean.class;
	String namedQueriesLocation() default "";
	QueryLookupStrategy.Key queryLookupStrategy() default QueryLookupStrategy.Key.CREATE_IF_NOT_FOUND;
	Class<?> repositoryBaseClass() default DefaultRepositoryBaseClass.class;
	String arangoTemplateRef() default "arangoTemplate";
}
