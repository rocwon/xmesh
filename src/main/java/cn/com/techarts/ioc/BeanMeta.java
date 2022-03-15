package cn.com.techarts.ioc;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

public class BeanMeta {
	private String name;
	private Class<?> clazz;
	private Object instance;
	private boolean singleton;
	private boolean assembled;
	private boolean independent = true;
	
	private Constructor<?> constructor;
	
	/**Contractor Arguments*/
	private Map<Integer, String> injectedArgs;
	
	/**Properties*/
	private Map<Field, String> injectedFields;
	
	public BeanMeta(String name, Class<?> clazz, boolean singleton) {
		this.name = name;
		this.clazz = clazz;
		this.singleton = singleton;
		this.injectedArgs = new HashMap<>();
		this.injectedFields = new HashMap<>();
		this.resolveInjectedFields();
		this.resolveInjectedContructors();
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Class<?> getClazz() {
		return clazz;
	}
	public void setClazz(Class<?> clazz) {
		this.clazz = clazz;
	}
	public Object getInstance() {
		return instance;
	}
	public void setInstance(Object instance) {
		this.instance = instance;
	}
	public boolean isSingleton() {
		return singleton;
	}
	public void setSingleton(boolean singleton) {
		this.singleton = singleton;
	}	
	public Map<Integer, String> getInjectedArgs(){
		return this.injectedArgs;
	}
	public Map<Field, String> getInjectedFields(){
		return this.injectedFields;
	}
	
	public Object newInstance(Object...args) {
		try {
			if(constructor != null) {
				return constructor.newInstance(args);
			}else {
				return clazz.getDeclaredConstructor().newInstance();
			}
		}catch(Exception e) {
			throw IocException.cannotInstance(this.name);
		}
	}
	
	private void resolveInjectedContructors() {
		var constructors = clazz.getConstructors();
		if(constructors == null) return; //Without?
		for(var c : constructors) {
			if(!c.isAnnotationPresent(Inject.class)) continue;
			var args = c.getParameterTypes();
			if(args == null || args.length == 0) break;
			this.constructor = c; //Cache it for new instance
			var annos = c.getParameterAnnotations();
			for(int i = 0; i < args.length; i++) {
				var name = args[i].getTypeName();
				var thisMethodIndependent = false;
				if(annos[i].length > 0) {
					if(annos[i][0] instanceof Named) { //REF
						thisMethodIndependent = false;
						name = ((Named)annos[i][0]).value();
					}else if(annos[i][0] instanceof Valued) { //KEY
						thisMethodIndependent =  true;
						name = ((Valued)annos[i][0]).key();
					}else {
						throw IocException.noQualifier(c.getName());
					}
				}
				if(name != null) {
					injectedArgs.put(Integer.valueOf(i), name);
					if(this.independent) {
						this.independent = thisMethodIndependent;
					}
				}
			}
			break; //At most 1 injected constructor in a class 
		}
	}
	
	private void resolveInjectedFields() {
		var theClass = clazz;
		while(theClass != null) {
			getInjectedFields(theClass);
			theClass = clazz.getSuperclass();
		}
	}
	
	private void getInjectedFields(Class<?> arg) {
		if(arg == null) return;
		var fs = arg.getDeclaredFields();
		if(fs == null || fs.length == 0) return;
		for(var f : fs) {
			if(!f.isAnnotationPresent(Inject.class)) continue;
			var name = f.getName();
			var thisFieldIndependent = false;
			var named = f.getAnnotation(Named.class);
			if(named != null) {
				name = named.value();
				thisFieldIndependent = false;
			}else {
				var valued = f.getAnnotation(Valued.class);
				if(valued != null) {
					name = valued.key();
					thisFieldIndependent = true;
				}
			}
			if(this.independent) {
				this.independent = thisFieldIndependent;
			}
			if(name != null) injectedFields.put(f, name);
		}
	}

	public Constructor<?> getConstructor() {
		return constructor;
	}

	public void setConstructor(Constructor<?> constructor) {
		this.constructor = constructor;
	}

	public boolean isIndependent() {
		return independent;
	}

	public void setIndependent(boolean independent) {
		this.independent = independent;
	}

	public boolean isAssembled() {
		return assembled;
	}

	public void setAssembled(boolean assembled) {
		this.assembled = assembled;
	}
	
//	private void getInjectedSetterMethods() {
//		var ms = clzz.getMethods();
//		if(ms == null || ms.length == 0) return;
//		for(var m : ms) {
//			if(!m.isAnnotationPresent(Inject.class)) continue;
//			var name = m.getName();
//			if(!name.startsWith("set")) continue;
//			name = populateFieldName(name);
//			var named = m.getAnnotation(Named.class);
//			if(named != null) name = named.value();
//			if(name != null) injectedMethods.put(m, name);
//		}
//	}
//	
//	private String populateFieldName(String name) {
//		var leading = name.substring(3, 1);
//		leading = leading.toLowerCase();
//		return leading.concat(name.substring(4));
//	}
}

