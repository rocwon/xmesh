package cn.com.techarts.msx;

import java.lang.reflect.Method;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import cn.techarts.jhelper.Empty;
import cn.techarts.jhelper.Spliter;

public final class ServiceDefinition {
	private M httpMethod;
	private Object object;
	private Method method;
	private String uri = null;
	
	public static boolean methodEnabled = false;
	
	public ServiceDefinition(String uri, Object object, Method method, M m) {
		this.uri = uri;
		this.httpMethod= m;
		this.object = object;
		this.method = method;
	}
	
	/**
	 * Get the last part of the whole URI
	 */
	public static String extractName(String uri) {
		if(Empty.is(uri)) return null;
		if(uri.indexOf('/') <= 0) return uri;
		var paths = Spliter.split(uri, '/');
		return paths.get(paths.size() - 1);
	}
	
	public Object getObject() {
		return object;
	}
	public void setObject(Object object) {
		this.object = object;
	}
	public Method getMethod() {
		return method;
	}
	public void setMethod(Method method) {
		this.method = method;
	}
	
	public void call(HttpServletRequest request, HttpServletResponse response, String m) {
		if(method == null || object == null) return;
		if(methodEnabled && httpMethod.match(m)) return;
		try {
			method.invoke(object, request, response);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public M getHttpMethod() {
		return httpMethod;
	}

	public void setHttpMethod(M httpMethod) {
		this.httpMethod = httpMethod;
	}
}
