package cn.com.techarts.msx;

import java.io.BufferedReader;
import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import cn.techarts.jhelper.Empty;

public class TechartsServlet extends HttpServlet 
{
	public static final String SERVICE = "f";
	public static final String USERID = "userId";
	
	public static final int INVALID_VCODE = -4;
	public static final int API_NOT_FOUND = -10086;
	public static final int UNKNOWN_CLIENT = -65535;
	 
	private WebApplicationContext context = null;
	public static final int TOO_MANY_DEVICE = -10010;
	public static final String USER_SESSION = "session";
	public static final String HEADER_USER_SESSION = "x:session";
	public static final String ASYNC_EXECUTOR = "webAsyncExecutor";
	public static final String SESSION = "RemotingServiceAuthorizationCode";
	public static final String CT_FORM_DATA = "application/x-www-form-urlencoded";
	
	@Override
	public void init(ServletConfig config) throws ServletException
	{
		super.init( config);
		var ctx = config.getServletContext();
		if( ctx != null){
			context = WebApplicationContextUtils
					  .getWebApplicationContext( ctx);
		}
	}
	
	public static int getUserAgent(HttpServletRequest request) {
		var agent = request.getHeader("user-agent");
		if(agent == null) {
			agent = request.getHeader("User-Agent");
		}
		return UserSession.getUserAgentType(agent).getId();
	}
	
	public void allowsCrossDomainAccess(HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "POST, GET");
        response.setHeader("Access-Control-Max-Age", "315360000"); //10 years
        response.setHeader("Access-Control-Allow-Headers", "x-requested-with");
    }
	
	/**
	 * Returns the full URI starting with a "/"(e.g. /user/login)<p>
	 * The parameter "f" has the highest priority. Namely, if the parser 
	 * found your request taking the parameter "f", it returned directly.
	 */
	public static String getServiceApi(HttpServletRequest req) {
		var api = req.getParameter(SERVICE);
		if(api == null) return req.getPathInfo();
		if(api.startsWith("/")) return api; // "f="/Login"
		return "/".concat(api); // Without the root path: "f=Login"
	}
	
	/**
	 * Get a java object from Spring IOC according to the given id and cast it to the specific type
	 */
	protected <T> T get( String id, Class<T> clzz)
	{
		try{
			if( context == null) return null;
			return context.getBean( id, clzz);
		}catch( Exception e){
			return null;
		}
	}
	
	public void redirect( HttpServletResponse response, String to)
	{
		try{ 
			response.sendRedirect(to);
		}catch( IOException e){ 
			
		}
	}
	
	public String getClientAddress(HttpServletRequest request) {
        var ip = request.getHeader("x-forwarded-for"); //Squid 
        if (Empty.is(ip) || ip.equalsIgnoreCase("unknown")) {  
            ip = request.getHeader("X-Real-IP"); //Nginx 
        }  
        if (Empty.is(ip) || ip.equalsIgnoreCase("unknown")) {  
            ip = request.getHeader("Proxy-Client-IP"); //Apache
        }  
        if (Empty.is(ip) || ip.equalsIgnoreCase("unknown")) {  
            ip = request.getRemoteAddr();  //Without Proxy
        }  
        return ip;  
    }  
	
	public String arg( HttpServletRequest request, String key){
		return request.getParameter( key);
	}
	
	public String getRealPath( String relativePath)
	{
		if( relativePath == null || context == null) return null;
		var servletContext = context.getServletContext();
		if(servletContext == null) return null;
		return servletContext.getRealPath( relativePath);
	}
	
	public void setSessionObject( HttpServletRequest request, String key, Object val){
		if( key == null || val == null) return;
		HttpSession session = request.getSession();
		if(session != null) session.setAttribute( key, val);
	}
	
	public<T> T getSessionObject( HttpServletRequest request, String key, Class<T> t){
		if( key == null) return null;
		HttpSession session = request.getSession();
		return session != null ? t.cast(session.getAttribute( key)) : null;
	}
	
	public String generateSessionCode(){
		return "";
	}
	
	public static String getRemorteAddress(HttpServletRequest request) {
		var result = request.getHeader("x-forwarded-for");
		if(result == null) result = request.getHeader("X-Real-IP");
		return result == null ? request.getRemoteAddr() : result;
	} 
	
	/*
	 * Important: The method just can be executed ONCE! The request body will be cleared after calling.
	*/
	public String getRequestBody(HttpServletRequest req) {
		String line = null;
		StringBuilder result = new StringBuilder(512);
		try{
			BufferedReader reader = req.getReader();
			if(reader == null) return ""; //Without
			while((line = reader.readLine()) != null){
				result.append(line);
			}
			if(reader != null) reader.close();
			return result.toString();
		}catch(IOException e) {return "";}
	}
	
	
	/**
	 * Just another implementation of {@link getRequestBody}
	 * Important: The method just can be executed ONCE! The request body will be cleared after calling.
	 * */
	@SuppressWarnings("resource")
	public String getRequestBody2(HttpServletRequest request) {
		int size = request.getContentLength();
		try {
			var content = request.getInputStream();
			if(content == null) return null; 
			byte[] contentBytes = new byte[size];
			content.read(contentBytes);
			return new String(contentBytes, "UTF-8");
		}catch(Exception e) {
			return null;
		}
	}
	
	public boolean isJsonBody(HttpServletRequest request) {
		var contentType = request.getContentType();
		if(Empty.is(contentType)) return false;
		return contentType.indexOf("json") >= 0;
	}
}