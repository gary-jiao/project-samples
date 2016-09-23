package com.test.events;

import java.lang.reflect.Field;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseFactory;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

/**
 * 记录方法的执行时间，会记录下Controller, Servicie, Repository里所有类的所有方法的调用时间。 <br/>
 * 目前所有操作历史记录都是保存H2数据库里，所以如果启用，项目需要增加H2的依赖。 <br/>
 * 如果需要启用此功能，需要在application.proerties增加配置选项 <br/>
 * method.event.enable = true     #控制是否开启此功能
 * method.event.db.path = /Users/gary/workspace/eventdb     #H2数据库的位置，如果没有配置此路径，则默认所有操作记录保存在内存里，重启系统后数据就没有了
 * @author gary
 *
 */
@Component
public class MethodExecutionLogger {

	private static Logger logger = LoggerFactory.getLogger(MethodExecutionLogger.class);

	private static EmbeddedDatabase db = null;
	private static NamedParameterJdbcTemplate template = null;
	
	@Value("${method.event.db.path:#{null}}")
	private String h2DbPath;

	private NamedParameterJdbcTemplate getTemplate() {
		try {
			if (db == null) {
				EmbeddedDatabaseBuilder builder = new EmbeddedDatabaseBuilder();
				if (StringUtils.isEmpty(h2DbPath)) {
					builder.setType(EmbeddedDatabaseType.H2);
				} else {
					Field field = builder.getClass().getDeclaredField("databaseFactory");
					field.setAccessible(true);
					EmbeddedDatabaseFactory factory = (EmbeddedDatabaseFactory)field.get(builder);
					factory.setDatabaseConfigurer(H2FileEmbeddedDatabaseConfigurer.getInstance().setDbPath(h2DbPath));
				}
				db = builder.setName("methodExecution").addScript("db/sql/create-db.sql").build();
			}
			if (template == null) {
				template = new NamedParameterJdbcTemplate(db);
			}
			return template;
		} catch (Exception ex) {
			logger.warn("cannot init embed database for logging method execution time", ex);
		}
		return null;
	}

	public void start(MethodExecutionModel model) {
		if (getTemplate() == null)
			return;

		String sql = "insert into app_method_execution_time (thread_name, class_name, method_name, start_time)";
		sql += " values (:threadName, :className, :methodName, :startTime) ";
		KeyHolder keyholder = new GeneratedKeyHolder();
		
		try {
			getTemplate().update(sql, new BeanPropertySqlParameterSource(model), keyholder);
			model.setOid(keyholder.getKey().longValue());
		} catch (Exception ex) {
			logger.error("cannot save execution time", ex);
		}
	}
	
	public void end(MethodExecutionModel model) {
		if (getTemplate() == null)
			return;
		
		String sql = "update app_method_execution_time ";
		sql += " set end_time = :endTime ";
		sql += " , duration = :duration ";
		sql += " , exec_status = :status ";
		sql += " where oid = :oid ";
		try {
			getTemplate().update(sql, new BeanPropertySqlParameterSource(model));
		} catch (Exception ex) {
			logger.error("cannot update execution time", ex);
		}
	}

}
