package cn.com.techarts.msx;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cn.com.techarts.msx.cluster.ClusterManager;
import cn.techarts.jhelper.Empty;

public class ServiceWarehouse {
	private static Map<Integer, Set<String>> inventory = new HashMap<>(10);
	private static Map<Integer, String> defaultServiceUrls = new HashMap<>(6);
	
	
	public static void register(int serviceCatalog, Set<String> services) {
		if(serviceCatalog == 0) return;
		if(!Empty.is(services)) {
			inventory.put(serviceCatalog, services);
		}
	}
	
	public static void register(int serviceCatalog, Class<?> serviceDeclarationClass, boolean all) {
		if(serviceCatalog == 0 || serviceDeclarationClass == null) return;
		var services = export(serviceDeclarationClass, all);
		if(!Empty.is(services)) inventory.put(serviceCatalog, services);
	}
	
	public static boolean is(int serviceCatalog, String service) {
		if(serviceCatalog == 0) return false;
		if(Empty.is(service)) return false;
		var ss = inventory.get(serviceCatalog);
		return ss != null ? ss.contains(service) : false;
	}
	
	public static boolean call(String api, HttpServletRequest request, HttpServletResponse response) {
		int catalog = getServiceCatalog(api);
		if(catalog == 0) return false; //The given API does not exist
		var serviceUrl = defaultServiceUrls.get(catalog);
		if(serviceUrl == null) {
			serviceUrl = ClusterManager.getServiceNode(catalog);
			defaultServiceUrls.put(catalog, serviceUrl);
		}
		ClusterManager.call(serviceUrl, request, response);
		return  true; //Here means the service is dispatched but does not mean it's successful.
	}
	
	public static boolean call(int serviceCatalog, String api, HttpServletRequest request, HttpServletResponse response) {
		if(!is(serviceCatalog, api)) return false;
		var serviceUrl = defaultServiceUrls.get(serviceCatalog);
		if(serviceUrl == null) {
			serviceUrl = ClusterManager.getServiceNode(serviceCatalog);
			defaultServiceUrls.put(serviceCatalog, serviceUrl);
		}
		ClusterManager.call(serviceUrl, request, response);
		return  true; //Here means the service is dispatched but does not mean it's successful.
	}
	
	private static int getServiceCatalog(String api) {
		for(var entry : inventory.entrySet()) {
			var apis = entry.getValue();
			if(!apis.contains(api)) continue;
			return entry.getKey(); //Service catalog ID
		}
		return 0; //Can't find the given API
	}
	
	public static void setDefaultServiceUrl(int serviceCatalog, String serviceUrl) {
		if(serviceCatalog == 0 || serviceUrl == null) return;
		defaultServiceUrls.put(serviceCatalog, serviceUrl);
	}
	
	private static Set<String> export(Class<?> service, boolean all) {
		Field[] fields = all ? service.getFields() : service.getDeclaredFields();
		if (fields == null || fields.length == 0)
			return Set.of();
		Set<String> result = new HashSet<>(512);
		try {
			for (Field field : fields) {
				if (field == null)
					continue;
				if (!field.getType().getName().contains("String"))
					continue;
				var name = field.get(null);
				if (name != null)
					result.add((String) name);
			}
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			return Set.of();
		}
	}
}