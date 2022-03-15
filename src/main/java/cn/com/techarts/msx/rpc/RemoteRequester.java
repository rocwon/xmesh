package cn.com.techarts.msx.rpc;

import java.util.Map;
import java.util.Set;
import java.util.List;
import java.util.HashMap;
import com.fasterxml.jackson.core.type.TypeReference;

import cn.techarts.jhelper.Empty;

public class RemoteRequester {
	private ServiceInvoker rpc = null;
	private boolean initialized = false;
	
	public RemoteRequester() {
		this.initialized = true;
	}
	
	public RemoteRequester(String serviceUrl) {
		init(serviceUrl);
	}
	
	public RemoteRequester(int serviceGroup) {
		this.initialized = true;
		rpc = new ServiceInvoker(serviceGroup);
	}
	
	private boolean init(String serviceUrl) {
		checkServiceUrl(serviceUrl);
		this.initialized = true;
		if(rpc != null) {
			rpc.setServiceUrl(serviceUrl);
		}else {
			rpc = new ServiceInvoker(serviceUrl);
		}
		return this.initialized;
	}
	
	public int getServiceGroup() {
		return rpc.getServiceGroup();
	}
	
	public void refreshServiceNodes() {
		if(rpc == null) return;
		rpc.refreshServiceNodes();
	}
	
	private void checkServiceUrl(String url) {
		if(!Empty.is(url)) return;
		throw new RuntimeException("\nFatal: Invalid service URL!\n");
	}
	
	public boolean initialized() {
		return this.initialized;
	}
	
	public String getServiceUrl() {
		return rpc.getServiceNodeUrl();
	}
	
	/**
	 * Call the method before each remote calling
	 */
	public boolean connect(String serviceUrl) {
		return init(serviceUrl);
	}
	
	public void resetServiceUrl(String service) {
		this.init(service);
	}
	
	public Map<String, String> create(String api){
		var result = new HashMap<String, String>(16);
		result.put("f", api);
		return result;
	}
	
	public Map<String, String> put(Map<String, String> param, String key, String val){
		if(key == null || val == null) return param;
		param.put(key, val);
		return param;
	}
	
	//---------------------------------Callers: fetch*------------------------------------------------
	
	public String fetch(Map<String, String> param) {
		RpcResult<Object> result = rpc.call(param);
		return result == null ? null : result.toStr();
	}
	
	public int fetchInteger(String json) {
		var result = rpc.call(json);
		return result != null ? result.toInt() : 0;
	}
	
	public int fetchInteger(Map<String, String> param) {
		var result = rpc.call(param);
		return result != null ? result.toInt() : 0;
	}
	
	public float fetchFloat(Map<String, String> param) {
		var result = rpc.call(param);
		return result != null ? result.toFloat() : 0f;
	}
	
	public String fetchString(Map<String, String> param) {
		var result = rpc.call(param);
		return result != null ? result.toStr() : null;
	}
	
	public <T> T fetchObject(Map<String, String> param, Class<T> t) {
		var result = rpc.call(param);
		return result != null ? result.toObj(t) : null;
	}
	
	/**
	 * @param type Like new TypeReference<List<Object[]>>(){};
	 */
	public <T> T[] fetchArray(Map<String, String> param, TypeReference<T[]> type) {
		var result = rpc.call(param);
		return result != null ? result.toArray(type) : null;
	}
	
	public int[] fetchIntArray(Map<String, String> param) {
		var result = rpc.call(param);
		return result != null ? result.toIntArray() : null;
	}
	
	/**
	 * Element: T
	 * @param t Like new TypeReference<List<Object>>(){};
	 */
	public <T> List<T> fetchObjects(Map<String, String> param, TypeReference<List<T>> t) {
		var result = rpc.call(param);
		return result != null ? result.toList(t) : null;
	}
	
	/**
	 * Element: Integer
	 */
	public List<Integer> fetchIntegers(Map<String, String> param) {
		var result = rpc.call(param);
		var type = new TypeReference<List<Integer>>(){};
		return result != null ? result.toList(type) : null;
	}
	
	/**
	 * Element: Integer
	 */
	public Set<Integer> fetchIntSet(Map<String, String> param) {
		var result = rpc.call(param);
		var type = new TypeReference<Set<Integer>>(){};
		return result != null ? result.toSet(type) : null;
	}
	
	/**
	 * Element: Integer
	 */
	public List<String> fetchStrings(Map<String, String> param) {
		var result = rpc.call(param);
		var type = new TypeReference<List<String>>(){};
		return result != null ? result.toList(type) : null;
	}
	
	/**
	 * Key: String, Value: String
	 */
	public Map<String, String> fetchMap(Map<String, String> param) {
		var result = rpc.call(param);
		return result != null ? result.toMap() : null;
	}
	
	/**
	 * Key: String, Value: Float
	 */
	public Map<String, Float> fetchFloatMap(Map<String, String> param) {
		var result = rpc.call(param);
		var type = new TypeReference<Map<String, Float>>(){};
		return result != null ? result.toStrMap(type) : null;
	}
	
	/**
	 * Key: Integer
	 * @param type Like new TypeReference<Map<Integer Object>>(){};
	 */
	public <V> Map<Integer, V> fetchIntMap(Map<String, String> param, TypeReference<Map<Integer, V>> type) {
		var result = rpc.call(param);
		return result != null ? result.toIntMap(type) : null;
	}
	
	/**
	 * Key: Integer
	 * @param type Like new TypeReference<Map<Integer Object>>(){};
	 */
	public <V> Map<String, V> fetchStrMap(Map<String, String> param, TypeReference<Map<String, V>> type) {
		var result = rpc.call(param);
		return result != null ? result.toStrMap(type) : null;
	}
}