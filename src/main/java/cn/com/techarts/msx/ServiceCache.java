package cn.com.techarts.msx;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cn.com.techarts.data.DaoHelper;
import cn.com.techarts.msx.cluster.ServiceNode;
import cn.com.techarts.msx.cluster.WhiteList;
import cn.techarts.jhelper.Cacher;
import cn.techarts.jhelper.Empty;

/**
 * It's a cache based on REDIS and caches cluster nodes, white-list and sessions.<p>
 * The cache works in a global scope and it's accessed by all services in cluster. 
 */
public class ServiceCache {
	private static int clusterCacheIndex = -1; //-1 means disabled
	private static int sessionCacheIndex = -1; //-1 means disabled
	private static Map<String, ServiceDefinition> webservices = null;
	
	public static void init(int clusterRedisIndex, int sessionRedisIndex) {
		clusterCacheIndex = clusterRedisIndex;
		sessionCacheIndex = sessionRedisIndex;
		webservices = new LinkedHashMap<>(512);
	}
	
	/**
	 * The caller must ensure that the parameters are legal
	 */
	public static void cacheService(String uri, ServiceDefinition service) {
		if(uri == null) return;
		if(service == null) return;
		webservices.put(uri, service);
	}
	
	/**
	 * The caller must ensure that the parameters are legal<p>
	 * There are 2 different APIs<p>
	 * <B><I>get</I></B>/user/login<br>
	 * <B><I>post</I></B>/user/login
	 */
	public static void cacheService(String uri, String method, ServiceDefinition service) {
		if(uri == null) return;
		if(method == null) return;
		if(service == null) return;
		webservices.put(method.toLowerCase().concat(uri), service);
	}
	
	/**For Example:<p> 
	 * get/login<p>
	 * post/users
	 */
	public static ServiceDefinition getService(String uri, String method) {
		if(Empty.is(uri)) return null;
		if(!ServiceDefinition.methodEnabled) {
			return webservices != null ? webservices.get(uri) : null; 
		}else {
			var m = method.toLowerCase().concat(uri);
			return webservices != null ? webservices.get(m) : null;
		}
	}
	
	public static Set<String> getServiceNames(){
		return webservices.keySet();
	}
	
	public static List<ServiceNode> reloadClusters(DaoHelper persister) {
		Cacher.clearCache(clusterCacheIndex);
		List<ServiceNode> nodes = persister.getAll("getClusterNodes", null);
		if(nodes == null || nodes.isEmpty()) return List.of();
		var groups = new HashMap<String, List<String>>(32);
		for(var node : nodes) {
			var group = groups.get(node.cacheKey());
			if(group == null) {
				group = new ArrayList<String>(5);
				groups.put(node.cacheKey(), group);
			}
			if(node.getStatus() != ServiceNode.ONLINE) continue;
			if(!Empty.is(node.getUrl())) group.add(node.getUrl());
		}
		for(var entry : groups.entrySet()) {
			if(entry.getValue().isEmpty()) continue;
			Cacher.saveList(clusterCacheIndex, entry.getKey(), entry.getValue(), 0);
		}
		
		List<WhiteList> whitelist = persister.getAll("getWhiteList", null);
		if(whitelist == null || whitelist.isEmpty()) return nodes;
		var whiteListMap = new HashMap<String, String>(32);
		for(var white : whitelist) {
			if(Empty.is(white.getIp())) continue;
			whiteListMap.put(white.getIp(), white.getServices());
		}
		Cacher.saveStrings(clusterCacheIndex, whiteListMap);
		
		return nodes; //Returns all service nodes for next refreshing each SDK
	}
	
	/**White-List<p>
	 * Detects the remote host( @param ip) is whether allowed to access the service( @param api)
	 */
	public static boolean isRequestAllowed(String ip, String api) {
		if(Empty.is(ip) || api == null) return false;
		return WhiteList.allows(Cacher.getString(clusterCacheIndex, ip), api);
	}
	
	public static List<String> getServers(int catalog){
		var key = ServiceNode.cacheKey(catalog);
		var result = Cacher.getList(clusterCacheIndex, key);
		return result != null ? result : List.of();
	}
	
	public static void refreshClusterNodes(List<String> nodes, int catalog) {
		var key = ServiceNode.cacheKey(catalog);
		Cacher.remove(clusterCacheIndex,  key);
		Cacher.saveList(clusterCacheIndex, key, nodes, 0);
	}
	
	public static void cacheSession(int userId, String ip, int agent, String session){
		if(sessionCacheIndex < 0) return;
		if(userId == 0 || session == null) return;
		String key = "XM_U_SESSION".concat(String.valueOf(userId));
		var exist = Cacher.getObject(sessionCacheIndex, key, UserSession.class);
		if(exist == null) {
			var us = new UserSession(agent, ip != null ? ip : "0000", session);
			Cacher.saveObject(sessionCacheIndex, key, us, UserSession.DURATION *60);
		}else {
			exist.appendNewDevice(session, agent);
			Cacher.saveObject(sessionCacheIndex, key, exist, UserSession.DURATION * 60);
		}
	}
	
	public static boolean checkSession(int userId, String ip, String session, int agent){
		if(sessionCacheIndex == -1) return true; //Ignored session comparing
		if(userId == 0 || session == null) return false;
		String key = "XM_U_SESSION".concat(String.valueOf(userId));
		var result = Cacher.getObject(sessionCacheIndex, key, UserSession.class);
		return result != null ? result.verify(agent, ip != null ? ip : "0000", session) : false;
	}
	
	public static void writeApiDoc(String name, String param, String type) {
		if(name == null) return;
		var key = "API_".concat(name);
		var params = Cacher.getMap(0, key);
		if(params != null) {
			if(params.containsKey(param)) return;
		}
		Cacher.setMapItem(0, key, param, type);
	}
	
	public static Map<String, Map<String, String>> getApiDocs(){
		var keys = Cacher.searchKeys(0, "API_*");
		if(Empty.is(keys)) return Map.of();
		var result = new HashMap<String, Map<String, String>>(1024);
		for(var key : keys) {
			result.put(key.substring(4), Cacher.getMap(0, key));
		}
		return result;
	}
}