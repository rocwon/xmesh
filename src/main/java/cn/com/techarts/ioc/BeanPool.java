package cn.com.techarts.ioc;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Named;
import javax.inject.Singleton;


public class BeanPool {
	
	public static final String NAME = "xmesh.ioc.app.cache";
	
	private Map<String, BeanMeta> beans;
	private Map<String, String> configs;
	
	public BeanPool() {
		beans = new HashMap<>(512);
		setConfigs(new HashMap<>(64));
	}
	
	public void register(String clzz) {
		var result = toBeanMeta(clzz);
		if(result != null) {
			beans.put(result.getName(), result);
		}
	}
	
	public <T> T getBean(String name, Class<T> t) {
		if(name == null) throw IocException.nullName();
		var bean = beans.get(name);
		if(bean == null) throw IocException.notFound(name);
		if(bean.isSingleton()) {
			return t.cast(bean.getInstance());
		}else {
			return t.cast(bean.newInstance());
		}
	}
	
	public Object getBean(String name) {
		if(name == null) {
			throw IocException.nullName();
		}
		var bean = beans.get(name);
		if(bean == null) {
			throw IocException.notFound(name);
		}
		if(bean.isSingleton()) {
			return bean.getInstance();
		}else {
			return bean.newInstance();
		}
	}
	
	private BeanMeta toBeanMeta(String className) {
		try {
			var obj = Class.forName(className);
			var name = obj.getSimpleName();
			var ann = obj.getAnnotation(Named.class);
			if(ann != null) name = ann.value();
			var singleton = obj.isAnnotationPresent(Singleton.class);
			if(ann == null && !singleton) return null;
			return new BeanMeta(name, obj, singleton);			
		}catch( ClassNotFoundException e) {
			throw IocException.notFound(className);
		}
	}
	
	private void scanAndResolve(String basePackage) {
		
	}
	
	private void instanceIndependentBeans() {
		for(BeanMeta bean : beans.values()) {
			if(bean.isIndependent()) {
				//bean.getConstructor().newInstance(initargs);
			}
		}
	}

	public Map<String, String> getConfigs() {
		return configs;
	}

	public void setConfigs(Map<String, String> configs) {
		this.configs = configs;
	}
	
	public String getConfig(String key) {
		if(configs == null) return null;
		var result = configs.get(key);
		if(result != null) return result;
		throw new RuntimeException("The config key [" + key + "] does not exist");
	}
}