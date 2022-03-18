package cn.com.techarts.msx;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import cn.com.techarts.msx.rpc.MsxClient;
import cn.com.techarts.util.FileHelper;
import cn.techarts.jhelper.Cacher;
import cn.techarts.jhelper.Empty;
import cn.techarts.jhelper.Executor;
import cn.techarts.jhelper.Spliter;

import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * If you extended the class and you will use it in your StartupListener, please
 * cast the property "settings" to the child class forcefully to continue your configuration.<p>
 * Another way, you also can get the setting object from spring IOC using the following method:<br>
 * {@link var settings = get(servletContext, "serviceSettings", YourChildClass.class)}
 */
public abstract class ServiceStartupListener implements ServletContextListener {
	protected ServiceSettings settings = null;
	private static final String ASYNC_EXECUTOR = "webAsyncExecutor";
	
	@Override
	public void contextInitialized(ServletContextEvent arg) {
		settings = get(arg.getServletContext(), "serviceSettings", ServiceSettings.class);
		if(settings == null) {
			throw new RuntimeException("#-------Service settings is required!-------#\n");
		}
		this.initializeRedisCacheService();
		this.initializeAysncMethodAndRequest(arg);
		initSessionSettings(settings); //About how to save and valid the session
		ServiceDefinition.methodEnabled = settings.isEnableHttpMethod(); // Get|Post
		ServiceCache.init(settings.getClusterRedisCache(), settings.getSessionRedisCache());
		this.loadAllServiceExporters(arg.getServletContext(), settings.getExporterPackage());
		MsxClient.init(settings.getRpcPoolMaxSize(), settings.getRpcConnPerRoute(), settings.isRpcKeepAlive());
	}

	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		Cacher.destroy();
		var executor = (ExecutorService) 
				 arg0.getServletContext().getAttribute(ASYNC_EXECUTOR);
		if(executor != null && !executor.isShutdown()) executor.shutdown();
	}
	
	private void initSessionSettings(ServiceSettings settings) {
		var key = settings.getSessionKey();
		var salt = settings.getSessionSalt();
		var duration = settings.getSessionDuration();
		var cacheable = settings.isSessionCacheable();
		UserSession.init(salt, duration, key, cacheable);
	}
	
	protected void initializeRedisCacheService() {
		var cacheSettings = settings.getCacheSettings();
		try {
			var param = Spliter.split(cacheSettings, ',');
			if(param == null || param.size() != 3) return;
			Cacher.init(param.get(0), param.get(1), param.get(2));
		}catch(RuntimeException e) {
			throw new RuntimeException("Failed to start service. " + e.getMessage());
		}
	}
	
	protected void initializeAysncMethodAndRequest(ServletContextEvent arg0) {
		if(settings.isAsyncMethodEnabled()) {
			Executor.enableAsyncMode();
		}	
		//Enable and initialize the web request thread pool
		int poolSize = settings.getRequestPoolSize();
		if(poolSize > 0) { //Enable ASYNC-SERVLET with a fix size thread pool
			var executor = Executors.newFixedThreadPool(poolSize);
			arg0.getServletContext().setAttribute(ASYNC_EXECUTOR, executor);
		}
	}
	
	/**
	 * @return Returns an object cached in spring IOC container.
	 */
	public<T> T get(ServletContext ctx, String id, Class<T> clzz)
	{
		if(ctx == null) return null;
		var context = WebApplicationContextUtils
				.getWebApplicationContext( ctx);
		try{
			if( context == null) return null;
			return context.getBean( id, clzz);
		}catch( Exception e){
			return null;
		}
	}
	
	/**
	 * @return Returns an object cached in spring IOC container.
	 */
	public Object get(ServletContext ctx, String id)
	{
		if(ctx == null) return null;
		var context = WebApplicationContextUtils
				.getWebApplicationContext( ctx);
		try{
			return context != null ? context.getBean(id) : null;
		}catch( Exception e){
			return null;
		}
	}
	
	private void loadAllServiceExporters(ServletContext context, String pkg) {
		var services = scanServiceExporters(context, pkg);
		//There is not any service need to be exported
		if(services == null || services.isEmpty()) return;
		for(var service : services) {
			var exporter = get(context, service);
			if(exporter == null) continue;
			var methods = exporter.getClass().getMethods();
			if(methods == null || methods.length == 0) continue;
			for(var method : methods) {
				var ws = AnnotationUtils.findAnnotation(method, WebService.class);
				//The method is not a (or not a legal) web service
				if(ws == null || ws.uri() == null) continue;
				var s = new ServiceDefinition(ws.uri(), exporter, method, ws.method());
				if(!ServiceDefinition.methodEnabled) {
					ServiceCache.cacheService(ws.uri(), s); // Without the prefix get|post 
				}else { //With a prefix string get|post such as get/user/login
					ServiceCache.cacheService(ws.uri(), ws.method().getName(), s);
				}
			}
		}
	}
	
	/**
	 * Scan the specific package path to retrieve all exporters<p>
	 * @TODO Now it can't process the sub-packages, we need to improve it later.
	 */
	private List<String> scanServiceExporters(ServletContext context, String pkg) {
		if(Empty.is(pkg)) return null;
		var base = getApplicationBasePath();
		if(Empty.is(base)) return null;
		var path = base.concat(pkg.replace('.', '/'));
		var files = FileHelper.poll(path, ".class");
		if(Empty.is(files)) return null;
		try {
			List<String> result = new ArrayList<>(24);
			for(var f : files) {
				if(f == null || !f.isFile()) continue;
				var n = f.getName().replace(".class", "");
				var c = Class.forName(pkg.concat(".").concat(n));
				if(c == null) continue;
				var a = c.getAnnotation(WebServiceExporter.class);
				if(a != null && a.name() != null) result.add(a.name());
			}
			return result;
		}catch(Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private String getApplicationBasePath() {
		var base = getClass().getResource("/");
		if(base == null || base.getPath() == null) return null;
		var w = File.separatorChar == '\\'; //Windows
		return w ? base.getPath().substring(1) : base.getPath();
	}
}