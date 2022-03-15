package cn.com.techarts.msx.rpc;

import java.util.Map;

import cn.com.techarts.msx.ServiceWarehouse;
import cn.com.techarts.msx.cluster.ClusterManager;
import cn.com.techarts.msx.cluster.FaultStrategy;
import cn.com.techarts.msx.codec.Codec;
import cn.com.techarts.msx.codec.Compact;
import cn.techarts.jhelper.Empty;

import java.util.List;
import java.util.ArrayList;

/**
 * Supporting 2 Encodings: JSON and MessagePack
 */
public class ServiceInvoker {
	private int serviceCatalog = 0;
	private String serviceUrl = null;
	private int maxNodes = 0, currentNode = -1;
	private List<String> serviceNodes = new ArrayList<>(8);
	
	public ServiceInvoker(String serviceUrl) {
		this.serviceUrl = serviceUrl;
	}
	
	public ServiceInvoker(int serviceCatalog) {
		this.serviceCatalog = serviceCatalog;
		this.refreshServiceNodes();
	}
	
	public int getServiceGroup() {
		return this.serviceCatalog;
	}
	
	public void refreshServiceNodes() {
		serviceNodes = ClusterManager.getServiceNodes(serviceCatalog);
		this.maxNodes = serviceNodes.size();
		if(this.maxNodes == 0) {
			System.out.println("#--Warning: No available service node\n");
		}
	}
	
	public void setServiceUrl(String serviceUrl) {
		this.serviceUrl = serviceUrl;
	}
	
	/**
	 * Returns a service node based on polling-algorithm (one by one)
	 */
	public String getServiceNodeUrl() {
		if(serviceUrl != null) return serviceUrl;
		if(maxNodes == 0) refreshServiceNodes();
		if(maxNodes == 0) { //A Fatal Error
			throw RemotingException.serviceless();
		}
		if(maxNodes == 1) return serviceNodes.get(0);
		return this.serviceNodes.get(getNodeIndex());
	}
	
	private int getNodeIndex() {
		if(++currentNode < maxNodes) {
			return this.currentNode;
		}else {
			this.currentNode = 0;
			return this.currentNode;
		}
	}
	
	// FaultStrategy.Failover is default
	private FaultStrategy getFaultStrategy(FaultStrategy... strategy) {
		if(strategy == null) return null;
		if(strategy.length == 0) return null;
		var def = FaultStrategy.Failover;
		return strategy[0] != null ? strategy[0] : def;
	}
	
	/**
	 * If you disabled MSGPACK encoding, the method just supports COMPACT-JSON encoding.<p>
	 * 
	 * If the HTTP 500 error has occurred, ignore fault strategy(An unrecoverable business exception).<p>
	 * IMPORTANT: We can simply call {@link cn.com.techarts.msx.rpc.bpost} to support MessagePack encoding.<p>
	 * 
	 * Actually, the method maybe throws a BasicDaoException if it's not unrecoverable,
	 * it notifies the caller to handle the transaction.
	 */
	public RpcResult<Object> call(Map<String, String> data, FaultStrategy... strategy) {
		var remoteUrl = this.getServiceNodeUrl();
		try {
			var result = MsxClient.post(remoteUrl, data, Compact.REDUCED);
			if(ServiceInvoker.hasError(result)) return null;
			return Codec.decodeRpcResult(result);
		}catch(RemotingException e) {
			return onRemotingException(e, data, null, remoteUrl, strategy);
		//}catch(BasicDaoException e) {
			//......
		}
	}
	
	/**
	 * If you disabled MSGPACK encoding, the method just supports COMPACT-JSON encoding.<p>
	 * 
	 * If HTTP 500 error has occurred, ignore fault strategy.<p>
	 * IMPORTANT: We can simply call {@link cn.com.techarts.msx.rpc.bpost} to support MessagePack encoding.
	 * 
	 * Actually, the method maybe throws a BasicDaoException if it's not unrecoverable,
	 * it notifies the caller to handle the transaction.
	 */
	public RpcResult<Object> call(String data, FaultStrategy... strategy) {
		var remoteUrl = this.getServiceNodeUrl();
		try {
			var result = MsxClient.post(remoteUrl, data, Compact.REDUCED);
			if(ServiceInvoker.hasError(result)) return null;		
			return Codec.decodeRpcResult(result);
		}catch(RemotingException e) {
			return onRemotingException(e, null, data, remoteUrl, strategy);
		}
	}
	
	/**
	 * @see private String onRemotingException()
	 */
	private RpcResult<Object> onRemotingException(RemotingException e, Map<String, String> param, String data, String lastUrl, FaultStrategy... strategy) {
		if(e.isFatalServiceException()) throw e;
		if(e.isBusinessLogicException()) throw e;
		var solution =getFaultStrategy(strategy);
		if(solution == FaultStrategy.Ignored) return null;
		var result = execFaultStrategy(param, data, lastUrl, solution);
		if(ServiceInvoker.hasError(result)) return null;	
		return Codec.decodeRpcResult(result);
	}
	
	/**
	 * IMPORTANT: We can simply call {@link cn.com.techarts.msx.rpc.bpost} to support MessagePack encoding
	 */
	private String execFaultStrategy(Map<String, String> param, String data, String lastUrl, FaultStrategy strategy) {
		this.refreshServiceNodes(); //Refresh service nodes
		String remoteUrl = lastUrl;
		if(strategy == FaultStrategy.Failover) {
			remoteUrl = this.getServiceNodeUrl();
			ServiceWarehouse.setDefaultServiceUrl(serviceCatalog, remoteUrl);
		}		
		try {
			if(!Empty.is(param)) {
				return MsxClient.post(remoteUrl, param, Compact.REDUCED);
			}else {
				return MsxClient.post(remoteUrl, data, Compact.REDUCED);
			}
		}catch(RemotingException e) {
			e.printStackTrace();
			return null; //Ignore the exception and return directly
		}
	}
	
	public static boolean hasError(String response) {
		if(Empty.is(response)) return true;
		return HttpConst.UNRECOVERABLES.contains(response.strip());
	}
	
	public static boolean hasError(byte[] response) {
		if(response == null) return true;
		if(response.length > 3) return false;
		if(response.length == 0) return true;
		var errcode = new String(response);
		return HttpConst.UNRECOVERABLES.contains(errcode);
	}
}