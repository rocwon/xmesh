package cn.com.techarts.msx.cluster;

import java.io.Serializable;
import java.util.Set;
import cn.techarts.jhelper.Finder;
import cn.techarts.jhelper.Spliter;

public class WhiteList implements Serializable{
	private String ip;
	private String services;
	private Set<String> apis;
	
	public String getIp() {
		return ip;
	}
	
	public void setIp(String ip) {
		this.ip = ip;
	}
	
	public String getServices() {
		return services;
	}
	
	public void setServices(String services) {
		this.services = services;
		var all = Spliter.split(services, ',');
		apis = all != null ? Set.copyOf(all) : Set.of();
	}
	
	public Set<String> getApis(){
		if(apis != null) return apis;
		return Set.of(); //An empty set
	}
		
	/**
	 * "/0" means all APIs are allowed to access.<p> 
	 * ("/API-") Ending with "-" means the API is denied.
	 * */
	public static boolean allows(String services, String api) {
		if(services == null || api == null) return false;
		if("/0".equals(services)) return true;
		var all = services.split(",");
		var index = Finder.find(all, api);
		if(index >= 0) return true;
		return Finder.find(all, api.concat("-")) < 0;
	}
}