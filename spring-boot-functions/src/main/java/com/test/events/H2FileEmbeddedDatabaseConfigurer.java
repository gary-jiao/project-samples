package com.test.events;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.datasource.embedded.ConnectionProperties;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseConfigurer;
import org.springframework.util.ClassUtils;

/**
 * 自定义的H2配置文件，Spring自带的只支持内存模式，应用一停止就没有了，这里扩展为文件模式，可以持久保留. <br/>
 *
 * @author gary
 *
 */
public class H2FileEmbeddedDatabaseConfigurer implements EmbeddedDatabaseConfigurer {
	protected final Log logger = LogFactory.getLog(getClass());
	
	private static H2FileEmbeddedDatabaseConfigurer instance;

	private final Class<? extends Driver> driverClass;
	private String dbPath;

	@Override
	public void shutdown(DataSource dataSource, String databaseName) {
		Connection con = null;
		try {
			con = dataSource.getConnection();
			con.createStatement().execute("SHUTDOWN");
		}
		catch (SQLException ex) {
			logger.warn("Could not shut down embedded database", ex);
		}
		finally {
			if (con != null) {
				try {
					con.close();
				}
				catch (Throwable ex) {
					logger.debug("Could not close JDBC Connection on shutdown", ex);
				}
			}
		}
	}


	/**
	 * Get the singleton {@code H2EmbeddedDatabaseConfigurer} instance.
	 * @return the configurer
	 * @throws ClassNotFoundException if H2 is not on the classpath
	 */
	@SuppressWarnings("unchecked")
	public static synchronized H2FileEmbeddedDatabaseConfigurer getInstance() throws ClassNotFoundException {
		if (instance == null) {
			instance = new H2FileEmbeddedDatabaseConfigurer( (Class<? extends Driver>)
					ClassUtils.forName("org.h2.Driver", H2FileEmbeddedDatabaseConfigurer.class.getClassLoader()));
		}
		return instance;
	}


	private H2FileEmbeddedDatabaseConfigurer(Class<? extends Driver> driverClass) {
		this.driverClass = driverClass;
	}

	public H2FileEmbeddedDatabaseConfigurer setDbPath(String dbPath) {
		this.dbPath = dbPath;
		return this;
	}
	
	public String getDbPath() {
		if (this.dbPath == null) {
			dbPath = System.getProperty("java.io.tmpdir");
		}
		return dbPath;
	}

	@Override
	public void configureConnectionProperties(ConnectionProperties properties, String databaseName) {
		properties.setDriverClass(this.driverClass);
		properties.setUrl(String.format("jdbc:h2:%s/%s;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=false", getDbPath(), databaseName));
		properties.setUsername("sa");
		properties.setPassword("");
	}
}
