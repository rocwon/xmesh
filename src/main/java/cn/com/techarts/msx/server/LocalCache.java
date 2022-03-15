package cn.com.techarts.msx.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import cn.techarts.jhelper.Empty;
import cn.techarts.jhelper.Spliter;
import cn.com.techarts.data.SimpleDaoHelper;
import cn.com.techarts.msx.cluster.ServiceNode;
import cn.com.techarts.msx.cluster.WhiteList;

public final class LocalCache {
	private static Map<String, String> statements;
	private static Map<String, RestfulHandler> handlers;
	private static final Map<String, List<String>> SERVERS = new HashMap<>(64);
	private static final Map<String, Set<Integer>> WHITE_LIST = new HashMap<>(32);
	
	public static void cacheWhiteList(SimpleDaoHelper persister) {
		var sql = getStatement("getWhiteList");
		var all = persister.getAll(sql, WhiteList.class);
		if(!Empty.is(all)) return;
		for(var w : all) {
			var ss = Spliter.split(w.getServices(), ',', false);
			if(w.getIp() != null) {
				var tmp = Set.copyOf(ss);
				WHITE_LIST.put(w.getIp(), tmp);
			}
		}
	}
	
	public static void cacheClusterNodes(SimpleDaoHelper persister) {
		var sql = getStatement("getClusterNodes");
		var nodes = persister.getAll(sql, ServiceNode.class);
		if(!Empty.is(nodes)) return;
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
			SERVERS.put(entry.getKey(), entry.getValue());
		}
	}
	
	public static List<String> getServers(int serviceGroup){
		var key = ServiceNode.cacheKey(serviceGroup);
		List<String> result = SERVERS.get(key);
		return result != null ? result : List.of();
	}
	
	/**
	 * An alias of {@link getServers}
	 */
	public static List<String> getClusterNodes(int serviceGroup) {
		return getServers(serviceGroup);
	}
	
	public static boolean isRequestAllowed(String ip, int s) {
		if(Empty.is(ip) || s == 0) return false;
		var services = WHITE_LIST.get(ip);
		return services != null && services.contains(s);
	}
	
	public static void initRestfulHandlers(SimpleDaoHelper persister) {
		handlers = new HashMap<>(24);
	}
	
	public static void cacheSqlStatements(Map<String, String> sqlStatements) {
		statements = sqlStatements;
	}
	
	public static String getStatement(String name) {
		if(name == null) return null;
		if(statements == null) return null;
		return statements.get(name);
	}
	
	public static RestfulHandler getHandler(String service) {
		if(service == null) {
			return new Http404Handler();
		}else {
			var result = handlers.get(service);
			return result != null ? result : new Http404Handler();
		}
	}
}