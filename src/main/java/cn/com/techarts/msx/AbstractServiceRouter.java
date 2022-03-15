package cn.com.techarts.msx;

import javax.servlet.AsyncContext;
import javax.servlet.ServletException;

import java.io.UnsupportedEncodingException;
import java.util.concurrent.ExecutorService;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import cn.techarts.jhelper.Converter;
import cn.techarts.jhelper.Empty;

/**
 * Exports all services over HTTP & JSON. The following parameters are required for each API call:<br>
 * <b>f</b>: A string identifies the API you want to call<br>
 * <b>userId</b>: A unique number of the current online user<br>
 * <b>session</b>: A string represents the unique identifier of current API call<br><p>
 * If the API(passed by parameter f) is not found, an error number(<b>-10086</b>) will be responded to caller.<br>
 * If an error number(<b>-10000</b>) is returned, that means the parameter <b>session</b> is illegal. <br>
 * This router supports ASYNC mode using a thread pool. It improves the performance(throughout) in concurrent environment.
 * If you set the thread pool size as ZERO, it will be switched to SYNC-mode.(Refers to the file web.xml -> "PoolSize").<p>
 * 
 * 2 JSON serialization solutions (ALIBABA FastJSON and JACKSON-2) are provided for choosing, we can switch the parser 
 * according to performance consideration. As default, we use JACKSON-2 because the spring-framework depends on it also.
 * And, according to our test, JACKSON(FasterJSON) is really faster than FastJSON(Caching the ObjectMapper for reusing).<p>
 * 
 * The API Caller includes the end users(userId is presented) and partners(in White List). 
 * The error number(<b>-10020</b>) will be returned if the caller is not a partner of your service.<p>
 * 
 * Recently Upgrade:<p>
 * Since 2020-03-07, the service supports 2 MIMES including JSON and FORM-DATA. <br>
 * Since 2020-10-08, the service supports MSGPACK protocol, raw JSON and compact-JSON.<br>
 * Since 2020-11-01, the service supports HTTP/2 protocol via Clear-Text(H2C).<br>
 * Since 2021-02-01, Move all features to an independent project "XMESH".
 */

public abstract class AbstractServiceRouter extends TechartsServlet implements SecurityManager{
	
	public int authenticate(HttpServletRequest req, HttpServletResponse response, String api){
		if(!ServiceSettings.P) return SecurityManager.ALLOWED;
		if(isAllowedAlways(api)) return SecurityManager.ALLOWED;
		String session = getSession(req), ip = getRemorteAddress(req);
		if(!Empty.is(session)) {//End User Entry
			int userId = Converter.toInt(req.getParameter(USERID));
			return checkSession(userId, ip,  getUserAgent(req), session);
		}else {	//White-List
			return isClientAllowed(ip, api) ? SecurityManager.ALLOWED : SecurityManager.DENIED;
		}
	}
	
	@Override
	public int checkSession(int user, String ip, int agent, String session) {
		boolean result = false;
		if(!UserSession.CACHEABLE) {
			result = UserSession.verify(ip, agent, user, session);
		}else {
			result = ServiceCache.checkSession(user, ip, session, agent);
		}
		return result ? SecurityManager.ALLOWED : SecurityManager.INVALID_SESSION;
	}
	
	private void setResponseCompactEncoding(HttpServletRequest request, HttpServletResponse response) {
		var compact = request.getHeader(ServiceExporter.COMPACT);
		if(compact == null) return; //RAW JSON WITHOUT COMPACT
		response.addHeader(ServiceExporter.COMPACT, compact);
	}
	
	private void setCharsetEncoding(HttpServletRequest request, HttpServletResponse response) {
		try {
			request.setCharacterEncoding("utf-8");
			response.setCharacterEncoding("utf-8");
		}catch(UnsupportedEncodingException e) {
			//You are a son of bitch if UTF8 is not supported.
		}
	}
	
	@Override
	public void service(HttpServletRequest request, HttpServletResponse response) throws ServletException{
		setCharsetEncoding(request, response);
		allowsCrossDomainAccess(response);
		String api = getServiceApi(request);
		
		int result = authenticate(request, response, api);
		this.setResponseCompactEncoding(request, response);
		
		if(api == null) {//Ping Request doesn't take any data
			ping(response, false);
			return; //The PING request has not furthermore actions
		}
		
		if(result != SecurityManager.ALLOWED) {
			ServiceExporter.respondNoQoS(response, result);
			return; //Request aborted and returned an error of permission
		}
		
		boolean jsonBody = false; //isJsonBody(request);
		var xrequest = JsonRequest.of(request, jsonBody);
		
		var executor = getThreadPool(request); //IS ASYNC?
		if(executor != null && !executor.isShutdown()) {
			AsyncContext context = request.startAsync();
			executor.execute(new Runnable() {
				@Override
				public void run(){
					handleRequestAsync(context, api, false); //isJsonBody
				}
			});
		}else { //Running as sync model
			handleRequestSync(xrequest, response, api);
		}
	}
	
	private void handleRequestAsync(AsyncContext context, String api, boolean json){
		context.setTimeout(300000); //It's long to 5 Minutes
		var request = (HttpServletRequest)context.getRequest();
		var response = (HttpServletResponse)context.getResponse();
		if(json) request = new JsonRequest(request);
		var method = request.getMethod();
		var service = ServiceCache.getService(api, method);
		if(service != null) {
			if(superviseQoS(request, response)) {
				service.call(request, response, method);
			}
		}else {
			if(!ServiceWarehouse.call(api, request, response)) {
				handleUndefinedRequest(api, request, response);
			}
		}		
		context.complete();
	}
	
	private void handleRequestSync(HttpServletRequest request, HttpServletResponse response, String api){
		var method = request.getMethod();
		var service = ServiceCache.getService(api, method);
		if(service != null) {
			if(superviseQoS(request, response)) {
				service.call(request, response, method);
			}
		}else {
			if(!ServiceWarehouse.call(api, request, response)) {
				handleUndefinedRequest(api, request, response);
			}
		}
	}

	private ExecutorService getThreadPool( HttpServletRequest request) {
		return (ExecutorService)request.getServletContext().getAttribute(ASYNC_EXECUTOR);
	}
	
	/**
	 * You can send the session in the request body(named "session") 
	 * or put it in request header with a customized name "x:session".
	 */
	private String getSession(HttpServletRequest request) {
		var session = request.getParameter(USER_SESSION);
		if(!Empty.is(session)) return session;
		return request.getHeader(HEADER_USER_SESSION);
	}
	
	/**
	 * @return 1: Synchronous Model; 2: Asynchronous Model
	 */
	private void ping(HttpServletResponse response, boolean async) {
		ServiceExporter.respondString(response, async ? "Async Mode" : "Sync Mode");
	}
	
	/**
	 * We can extend the method to monitor more indicators (e.g. request counting), if necessary.
	 * If the request is discarded or fused, the status will be set as TIMEOUT. According to your
	 * fault-strategy, the further process will be performed.
	 * 
	 */
	private boolean superviseQoS(HttpServletRequest request, HttpServletResponse response){
		var qos = get(ServiceSupervisor.CACHE_KEY, ServiceSupervisor.class);
		var action = qos == null ? ServiceSupervisor.PROCESS : qos.income().limits();
		if(action == ServiceSupervisor.DISCARD) {
			response.setStatus(HttpServletResponse.SC_REQUEST_TIMEOUT);
			ServiceExporter.respondNoQoS(response, ServiceSupervisor.ERRNO_DISCARD);
			return false;
		}else if(action == ServiceSupervisor.BREAK) {
			response.setStatus(HttpServletResponse.SC_REQUEST_TIMEOUT);
			ServiceExporter.respondNoQoS(response, ServiceSupervisor.ERRNO_BREAK);
			return false;
		}
		return true;
	}
	
	/**
	 * If a service request can't be found in cache(Undefined), you should manually handle it in your project.<br>
	 * Typically, it's easy to implement a load-balance server(Inverse PROXY) using this mechanism.
	 */
	protected abstract void handleUndefinedRequest(String api, HttpServletRequest request, HttpServletResponse response);
}