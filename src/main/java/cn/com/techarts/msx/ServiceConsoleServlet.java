package cn.com.techarts.msx;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cn.com.techarts.data.DaoHelper;
import cn.com.techarts.msx.cluster.ServiceNode;
import cn.com.techarts.msx.rpc.MsxClient;
import cn.techarts.jhelper.Empty;

public class ServiceConsoleServlet extends TechartsServlet 
{
	public boolean checkPermission(HttpServletRequest req, HttpServletResponse response){
		return true;
	}
	
	@SuppressWarnings("resource")
	@Override
	public void service( HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
		response.setCharacterEncoding("utf-8");
		this.allowsCrossDomainAccess(response);
		if(validate(request)) {
			switch(request.getParameter("cmd")) {
				case "refreshServers":
					refreshServiceNode(); //Blade
					response.getWriter().write("1");
					break;
				case "testServers":
					ServiceExporter.respondsJson(response, testClusterNodes(), false);
					break;
				default:
					response.getWriter().write("-1"); 
					break;
				}
		}else {
			response.getWriter().write("-40"); //Permission is denied
		}
		response.getWriter().flush();
	}
	
	private boolean validate(HttpServletRequest request) {
		var ip = request.getRemoteAddr();
		if(!"112.64.196.42".equals(ip)) return false;
		return "20100308".equals(request.getParameter("token"));
	}
	
	/**
	 * Reload all service nodes from database(blade.sys_servers, blade.sys_whitelist)
	 * And then, refresh services of each catalog for all SDK
	 */
	private void refreshServiceNode() {
		var daoHelper = get("daoHelper", DaoHelper.class);
		var servers = ServiceCache.reloadClusters(daoHelper);
		if(servers == null || servers.isEmpty()) return;
		for(var server : servers) {
			var url = server.getUrl();
			if(Empty.is(url)) continue;
			MsxClient.post(url.concat("/RefreshServers"));
		}
	}
	
	private Map<String, Integer> testClusterNodes(){
		var persister = get("daoHelper", DaoHelper.class);
		if(persister == null) return Map.of();
		List<ServiceNode>  all = persister.getAll("getClusterNodes", null);
		if(Empty.is(all)) return Map.of();
		Map<String, Integer>  result = new HashMap<>();
		for(var node : all) {
			if(node == null || Empty.is(node.getUrl())) continue;
			result.put(node.showName(), MsxClient.ping(node.getUrl()));
		}
		return result;
	}
}