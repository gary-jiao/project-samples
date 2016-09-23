package com.test.repository;

import java.util.List;
import java.util.Map;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface UserQuery extends CrudRepository<User, Long> {
	
	/**
	 * 查询只返回id和username，不要其他属性 <br/>
	 * 这个演示的方法其实用Spring data自带的功能就能完成，这里只是为了演示而已
	 * @param username
	 * @return
	 */
	@Query(value = "select oid, username from tbUser where username = :username", name="java.util.HashMap", nativeQuery = true)
	List<Map> findUsers(@Param("username") String username);
}
