package cn.com.techarts.msx;

import cn.techarts.jhelper.Converter;
import cn.techarts.jhelper.Spliter;

public class ServiceSettings {
	/**
	 * An alias of permission
	 */
	public static boolean P = false;
	private boolean permission;
	private boolean masterNode;
	private String sessionKey;
	private String sessionSalt;
	private int sessionDuration;
	private int requestPoolSize;
	private String cacheSettings;
	private String exporterPackage;
	private boolean enableHttpMethod;
	
	private boolean asyncMethodEnabled;
	private boolean rpcKeepAlive = false;
	private int rpcPoolMaxSize = 128, rpcConnPerRoute = 32;
	private int clusterRedisCache = -1, sessionRedisCache = -1;
	
	public boolean isMasterNode() {
		return masterNode;
		
	}
	public void setMasterNode(String masterNode) {
		this.masterNode = Converter.toBoolean(masterNode);
	}
	public int getRequestPoolSize() {
		return requestPoolSize;
	}
	public void setRequestPoolSize(String requestPoolSize) {
		this.requestPoolSize = Converter.toInt(requestPoolSize);
	}
	public String getCacheSettings() {
		return cacheSettings;
	}
	public void setCacheSettings(String cacheSettings) {
		this.cacheSettings = cacheSettings;
	}
	public boolean isAsyncMethodEnabled() {
		return asyncMethodEnabled;
	}
	public void setAsyncMethodEnabled(String asyncMethodEnabled) {
		this.asyncMethodEnabled = Converter.toBoolean(asyncMethodEnabled);
	}
	
	public boolean isPermission() {
		return permission;
	}
	public void setPermission(boolean permission) {
		P = permission;
		this.permission = permission;
	}
	public int getRpcPoolMaxSize() {
		return rpcPoolMaxSize;
	}
	public void setRpcPoolMaxSize(int rpcPoolSize) {
		this.rpcPoolMaxSize = rpcPoolSize;
	}
	public int getRpcConnPerRoute() {
		return rpcConnPerRoute;
	}
	public void setRpcConnPerRoute(int rpcConnPerRoute) {
		this.rpcConnPerRoute = rpcConnPerRoute;
	}
	public void setRpcPoolSettings(String poolSettings) {
		var settings = Spliter.split(poolSettings, ',', false);
		if(settings == null || settings.size() != 3) return;
		this.rpcKeepAlive = settings.get(2) != 0; // 0 == false
		if(settings.get(0) > 0) this.rpcPoolMaxSize = settings.get(0);
		if(settings.get(1) > 0) this.rpcConnPerRoute = settings.get(1);
	}
	public boolean isRpcKeepAlive() {
		return rpcKeepAlive;
	}
	public void setRpcKeepAlive(boolean rpcKeepAlive) {
		this.rpcKeepAlive = rpcKeepAlive;
	}
	public int getClusterRedisCache() {
		return clusterRedisCache;
	}
	public void setClusterRedisCache(int clusterRedisCache) {
		this.clusterRedisCache = clusterRedisCache;
	}
	public int getSessionRedisCache() {
		return sessionRedisCache;
	}
	public void setSessionRedisCache(int sessionRedisCache) {
		this.sessionRedisCache = sessionRedisCache;
	}
	public String getSessionSalt() {
		if(sessionSalt != null) return sessionSalt;
		return "LikeaBridg3overtR0ub1eDwaTer"; //Default
	}
	public void setSessionSalt(String sessionSalt) {
		this.sessionSalt = sessionSalt;
	}
	public int getSessionDuration() {
		return sessionDuration;
	}
	public void setSessionDuration(int sessionDuration) {
		this.sessionDuration = sessionDuration;
	}
	public String getSessionKey() {
		return sessionKey;
	}
	public void setSessionKey(String sessionKey) {
		this.sessionKey = sessionKey;
	}
	
	public boolean isSessionCacheable() {
		return this.sessionRedisCache >= 0;
	}
	public String getExporterPackage() {
		return exporterPackage;
	}
	public void setExporterPackage(String exporterPackage) {
		this.exporterPackage = exporterPackage;
	}
	public boolean isEnableHttpMethod() {
		return enableHttpMethod;
	}
	public void setEnableHttpMethod(String enableHttpMethod) {
		this.enableHttpMethod = "1".equals(enableHttpMethod);
	}
}