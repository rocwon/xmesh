package cn.com.techarts.data;

import java.util.List;
import java.sql.SQLException;
import com.zaxxer.hikari.HikariConfig;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;

/**
 * A lightweight utility that's designed to access database 
 * based on APACHE DBUTILS and HIKARI connection pool.
 */
public final class SimpleDaoHelper{
	private TechartsDataSource dataSource = null;
	
	public SimpleDaoHelper(TechartsDataSource dataSource) {
		this.dataSource = dataSource;
	}
	
	public SimpleDaoHelper(String driver, String url, String user, String token) {
		this.prepareDataSource(driver, url, user, token);
	}
	
	public void close() {
		if(dataSource == null) return;
		this.dataSource.close(); //Shutdown
	}
	
	private void prepareDataSource(String driver, String url, String user, String token) {
		var config = new HikariConfig();
		config.setJdbcUrl(url);
		config.setUsername(user);
		config.setPassword(token);
		config.setDriverClassName(driver);
		//config.setDataSourceClassName(driver);
		dataSource = new TechartsDataSource(config);
	}
	
	public QueryRunner getExecutor() {
		if(dataSource == null) return null;
		return new QueryRunner(this.dataSource);
	}
	
	/**
	 * The method is designed to handle the INSERT, UPDATE, DELETE statements 
	 */
	public int update(String sql, Object... params) throws RuntimeException{
		if(sql == null) return -1;
		try {
			getExecutor().update(sql, params);
			return 0;
		}catch(SQLException e) {
			throw new RuntimeException("Failed save data.", e);
		}
	}
	
	/**
	 * Returns a single object
	 */
	public<T> T get(String sql, Class<T> classOfTarget, Object... params) throws RuntimeException{
		if(sql == null || classOfTarget == null) return null;
		try {
			var target = new BeanHandler<T>(classOfTarget);
			return getExecutor().query(sql, target, params);
		}catch(SQLException e) {
			throw new RuntimeException("Failed to search data with SQL[" + sql + "]", e);
		}
	}
	
	/**
	 * Returns a set of objects
	 */
	public<T> List<T> getAll(String sql, Class<T> classOfTarget, Object... params)  throws RuntimeException{
		if(sql == null || classOfTarget == null) return null;
		try {
			var target = new BeanListHandler<T>(classOfTarget);
			return getExecutor().query(sql, target, params);
		}catch(SQLException e) {
			throw new RuntimeException("Failed to search data with SQL[" + sql + "]", e);
		}
	}	
}