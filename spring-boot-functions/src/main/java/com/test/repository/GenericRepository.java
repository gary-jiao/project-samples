package com.test.repository;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.Repository;

@NoRepositoryBean
public interface GenericRepository extends Repository<Model, Serializable> {

	<C> C findByKey(Class<? extends Model> clazz, Serializable key);
	
	<C> List<C> findByFields(Class<? extends Model> clazz, Model model, String... fields);
	
	<C> C findUniqueResultByFields(Class<? extends Model> clazz, Model model, String... fields);
	
	@SuppressWarnings("rawtypes")
	List<Map> findResultWithMap(String sql, QueryCriteria... criteria) ;
	
	<C> List<C> findResults(Class<C> clazz, String sql, QueryCriteria... criteria); 
	
	void save(Model entity);
	
	void update(Model entity);
}
