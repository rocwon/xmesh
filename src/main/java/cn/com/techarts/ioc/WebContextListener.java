package cn.com.techarts.ioc;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class WebContextListener implements ServletContextListener {
	
	public static final String CONFIG_PATH = "contextConfigLocation";
	
	@Override
	public void contextInitialized(ServletContextEvent sce) {
		var context = sce.getServletContext();
		var objectContainer = new BeanPool();
		context.setAttribute(BeanPool.NAME, objectContainer);
		objectContainer.setConfigs(resolveConfiguration(context));
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		// TODO Auto-generated method stub
		
	}
	
	private Map<String, String> resolveConfiguration(ServletContext context) {
		var path =  context.getRealPath("/"); //Root of the application
		var file = context.getInitParameter(CONFIG_PATH);
		if(file == null || "".equals(file.trim())) return null;
		
		if(!file.startsWith("/")) {
			path = path.concat(file);
		}else {
			path = path.concat(file.substring(1));
		}
		Properties config = new Properties();
		var result = new HashMap<String, String>(32);
		try {
			var inStream = new FileInputStream(path);
			config.load(inStream);
			for(var key : config.stringPropertyNames()) {
				result.put(key, config.getProperty(key));
			}
			inStream.close();
			return result;
		}catch(IOException e) {
			throw new RuntimeException("Failed to load the configuration [" + path + "]");
		}
	}

}
