package cn.com.techarts.msx.cluster;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import cn.com.techarts.msx.JsonRequest;
import cn.com.techarts.msx.ServiceCache;
import cn.com.techarts.msx.ServiceExporter;
import cn.com.techarts.msx.TechartsServlet;
import cn.com.techarts.msx.codec.Compact;
import cn.com.techarts.msx.rpc.MsxClient;
import cn.com.techarts.msx.rpc.RemotingException;
import cn.com.techarts.msx.rpc.HttpConst;
import cn.techarts.jhelper.Converter;
import cn.techarts.jhelper.Empty;

public final class ClusterManager {
	public static final int A_RANDOM = 1, A_BALANCE = 2;
	public static final String SERVERS = "ClusterNodes";
	
	public static String getServiceNode(int serviceGroup) {
		return getRandomServiceNode(serviceGroup);
	}
	
	public static List<String> getServiceNodes(int serviceGroup){
		var nodes = ServiceCache.getServers(serviceGroup);
		return nodes != null ? nodes : new ArrayList<>();
	}
	
	private static String getRandomServiceNode(int service) {
		var nodes = ServiceCache.getServers( service);
		if(nodes == null || nodes.isEmpty()) return null;
		int size = nodes.size();
		if(size == 1) return nodes.get(0);
		return nodes.get(new Random().nextInt(size));
	}
	
	public static boolean isNodeAlive(ServiceNode node) {
		if(Empty.is(node.getUrl())) return false;
		if(new Random().nextInt(22) % 3 == 0) {
			try {
				if(ping(node.getUrl())) {
					node.setStatus(ServiceNode.ONLINE);
				}else {
					node.setStatus(ServiceNode.OFFLINE);
				}
			}catch(RuntimeException e) {
				node.setStatus(ServiceNode.OFFLINE);
			}
			return node.getStatus() == ServiceNode.ONLINE;
		}else {
			return true;
		}
	}
	
	public static boolean ping(String url) {
		return MsxClient.ping(url) == HttpConst.SC_OK;
	}
	
	public static void call(int serviceGroup, HttpServletRequest request, HttpServletResponse response) {
		call(ClusterManager.getServiceNode(serviceGroup), request, response);
	}
	
	public static void call(String serviceUrl, HttpServletRequest request, HttpServletResponse response) {
		if(serviceUrl == null) throw new RemotingException(503);
		if(serviceUrl.startsWith("/")) {
			serviceUrl = serviceUrl.substring(1);
		}
		var api = TechartsServlet.getServiceApi(request);
		serviceUrl = serviceUrl.concat(api); // "../services/Login"
		forwards(serviceUrl, request, response, false);
	}
	
	/***
	 * ReverseProxy (Copy request parameters one by one)
	 */
	private static void forwards(String url, HttpServletRequest request, HttpServletResponse response, boolean withSession) {
		String result = null;
		byte[] bresult = null;
		Compact compact = getCompact(request);
		if(request instanceof JsonRequest) {
			var body = ((JsonRequest)request).getRawJson(withSession);
			if(compact == Compact.MSGPACK) {
				bresult = MsxClient.bpost(url, body, true);
			}else {
				result = MsxClient.postJson(url, body, compact);
			}
		}else {
			var body = getRequestParameters(request, withSession);
			if(compact == Compact.MSGPACK) {
				bresult = MsxClient.bpost(url, body, false);
			}else {
				result = MsxClient.post(url, body, compact);
			}
		}
		
		if(compact == Compact.MSGPACK) {
			ServiceExporter.respondBinary(response, bresult);
		}else {
			ServiceExporter.respondString(response, result);
		}
	}
	
	/***
	 * Reverse Proxy(Copy input-stream directly)<p>
	 * The binary version of above {@link forward}
	 * @param url
	 * @param request
	 * @param response
	 */
	@SuppressWarnings("resource")
	public static void forward(String url, HttpServletRequest request, HttpServletResponse response) {
		Compact compact = getCompact(request);
		String contentType = request.getContentType();
		try {
			if(compact == Compact.MSGPACK) {
				var result = MsxClient.bpost(url, request.getInputStream(),false, contentType);
				ServiceExporter.respondBinary(response, result);
			}else {
				var result = MsxClient.post(url, request.getInputStream(), compact, contentType);
				ServiceExporter.respondString(response, result);
			}			
		}catch(IOException e) {
			throw new RemotingException(e.getMessage());
		}
	}
	
	private static Compact getCompact(HttpServletRequest request) {
		var compact = Converter.toInt(request.getHeader(ServiceExporter.COMPACT));
		if(compact == Compact.RAW_JSON_STRING) return Compact.RAW;
		if(compact == Compact.BINARY_MSG_PACK) return Compact.MSGPACK;
		return (compact == Compact.IGNORE_DEF_VALS) ? Compact.REDUCED : Compact.RAW;
	}
	
	private static Map<String, String> getRequestParameters(HttpServletRequest request, boolean withSession){
		var result = new HashMap<String, String>();
		var params = request.getParameterMap();
		if(Empty.is(params)) return null;
		for(var entry : params.entrySet()) {
			var vals = entry.getValue();
			if(Empty.is(vals)) continue;
			result.put(entry.getKey(), vals[0]);
		}
		if(!withSession) result.remove(TechartsServlet.USER_SESSION);
		return result.isEmpty() ? null : result;
	}
}