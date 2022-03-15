package cn.com.techarts.data;

import java.util.Properties;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import cn.techarts.jhelper.Cryptor;

public class TechartsDataSource extends HikariDataSource {
	
	private static final String KEY = "b67fe6a8a28e2729c196deb99e6afd60";
	
	public TechartsDataSource() {
		super();
	}
	
	public TechartsDataSource(HikariConfig config) {
		super(config);
	}
	
	private static String encrypt(String password) {
		var key = Cryptor.toBytes(KEY);
		return Cryptor.decrypt(password, key);
	}
	
	@Override
	public void addDataSourceProperty(String propertyName, Object value){
		if("password".equals(propertyName)) {
			var val = encrypt((String)value);
			super.addDataSourceProperty("password", val);
		}else {
			super.addDataSourceProperty(propertyName, value);
		}
	}
	
	@Override
	public void setDataSourceProperties(Properties dsProperties){
		var pwd = dsProperties.getProperty("password");
		if(pwd != null) {
			dsProperties.setProperty("password", encrypt(pwd));
		}
		super.setDataSourceProperties(dsProperties);
	}
	
	public static String pwd(String encrypted, String token) {
		return "asdf!@#$".equals(token) ? encrypt(encrypted) : null;
	}
	
	public static void main(String[] args) {
		System.out.println(encrypt("654d80091287459696fb136f6d7e5b1a"));
	}
}