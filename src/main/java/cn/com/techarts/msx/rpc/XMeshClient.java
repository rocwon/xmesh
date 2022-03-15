package cn.com.techarts.msx.rpc;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cn.com.techarts.msx.cluster.ClusterManager;

/**
 * An class implements the fundamental mechanism of service calling within the cluster nodes.
 */
public class XMeshClient extends RemoteRequester{
	
	public XMeshClient(int serviceGroup) {
		super(serviceGroup);
		if(!initialized()) {
			throw new RuntimeException("Failed to init client: " + serviceGroup);
		}
	}	
	
	public XMeshClient(String serviceUrl) {
		super(serviceUrl);
		if(!initialized()) {
			throw new RuntimeException("Failed to init client: " + serviceUrl);
		}
	}
	
	/**
	 * <b><i>Transparent Transmission</i></b><p>
	 *Forward the request to another project(one of servers in the cluster) 
	 * 
	 */
	public void call(HttpServletRequest request, HttpServletResponse response) {
		var serviceUrl = getServiceUrl();
		if(serviceUrl == null) {
			int group = getServiceGroup();
			serviceUrl = ClusterManager.getServiceNode(group);
		}
		ClusterManager.call(serviceUrl, request, response);
	}
}