package cn.com.techarts.msx.cluster;

import cn.com.techarts.basic.StatefulObject;

public class ServiceNode extends StatefulObject
{
	private String url;
	private int master;
	private int clients;
	
	/**
	 * A group contains a series same services. <p>
	 * When a request is coming, the load-balancer 
	 * dispatches it to one of services in the group
	 * according to a specific load-balance algorithm.
	 */
	private int serviceGroup;
	
	public static final int ONLINE = 1;
	public static final int OFFLINE = 0;
	
	public ServiceNode() {}
	
	public int getClients() {
		return clients;
	}
	public void setClients(int clients) {
		this.clients = clients;
	}
	
	public ServiceNode addClient() {
		this.clients += 1;
		return this;
	}
	
	public void reduceClient() {
		this.clients -= 1;
	}

	public int getMaster() {
		return master;
	}

	public void setMaster(int master) {
		this.master = master;
	}
	
	public boolean masterNode() {
		return this.master == 1;
	}
	
	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public int getServiceGroup() {
		return serviceGroup;
	}

	public void setServiceGroup(int group) {
		this.serviceGroup = group;
	}
	
	public String cacheKey() {
		return new StringBuilder("CLUSTER_0_").append(serviceGroup).toString();
	}
	
	public static String cacheKey(int serviceGroup) {
		return new StringBuilder("CLUSTER_0_").append(serviceGroup).toString();
	}
	
	public String showName() {
		return getName() + "-" + url;
	}
}