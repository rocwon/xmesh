package cn.com.techarts.msx;

import java.io.IOException;
import java.util.Date;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cn.com.techarts.basic.Results;
import cn.com.techarts.msx.codec.Codec;
import cn.com.techarts.msx.codec.Compact;
import cn.com.techarts.msx.rpc.RpcResult;
import cn.techarts.jhelper.Converter;
import cn.techarts.jhelper.Empty;
import cn.techarts.jhelper.Time;

public abstract class ServiceExporter {
	private ServiceSupervisor serviceSupervisor = null;
	private static Map<Integer, String> ERRINFO = null;
	public static final String WEBSERVICES = "xmesh_webservices";
	public static final String CT_BIN = "application/octet-stream";
	public static final String CT_TEXT = "text/plain;charset=UTF-8";
	public static final String COMPACT = "response-content-compact";
	public static final String PARAM_USERID = "userId", PARAM_OWNER = "owner", PARAM_ID = "id";
	
	public ServiceExporter() {}
	
	/**
	 * Required 
	 */
	protected void setErrorMessages(Map<Integer, String> errinfo) {
		ERRINFO = errinfo;
	}
	
	public void setServiceSupervisor(ServiceSupervisor serviceSupervisor) {
		this.serviceSupervisor = serviceSupervisor;
	}
	
	private void supervise(int status) {
		if(serviceSupervisor == null) return;
		switch(status) {
			case 408: //REQUEST TIMEOUT
			case 503: //SERVICE UNAVAILIABLE
			case 504: //GATEWAY TIMEOUT
				serviceSupervisor.failed();
		}
	}
	
	private static Map<Integer, String> getErrinfo() {
		if(ERRINFO != null) {
			return ERRINFO;
		}
		ERRINFO = Map.of();
		return ERRINFO;
	}
	
	public<T> void responds(HttpServletResponse response, T result) {
		var hc = response.getHeader(COMPACT);
		var compact = Converter.toInt(hc); //header_compact
		if(compact == 0) { //RAW JSON
			respondsAsJson(response, result, false);
		}else if(compact == Compact.IGNORE_DEF_VALS) {
			respondsAsJson(response, result, true);
		}else if(compact == Compact.BINARY_MSG_PACK) {
			respondsAsMessagePack(response, result);
		}else {//Otherwise numbers for extending later
			respondsAsJson(response, result, false);
		}
	}
	
	/**
	 * Without QoS
	 */
	public static<T> void respondNoQoS(HttpServletResponse response, T result) {
		var hc = response.getHeader(COMPACT);
		var compact = Converter.toInt(hc); //header_compact
		if(compact == 0) { //RAW JSON
			respondsJson(response, result, false);
		}else if(compact == Compact.IGNORE_DEF_VALS) {
			respondsJson(response, result, true);
		}else if(compact == Compact.BINARY_MSG_PACK) {
			respondsMessagePack(response, result);
		}else { //Otherwise numbers for extending later
			respondsJson(response, result, false);
		}
	}
	
	public void respondsAsJson(HttpServletResponse response, Object result, boolean compact){
		supervise(response.getStatus());
		respondsJson(response, result, compact);
	}
	
	@SuppressWarnings("resource")
	public static void respondsJson(HttpServletResponse response, Object result, boolean compact){
		try{
			response.setContentType(CT_TEXT);
			Integer retval = errno(result);
			var content = Codec.encode(result, compact);
			content = std(retval, content);
			response.getWriter().write(content);
			response.getWriter().flush();
		}catch(IOException e){ 
			e.printStackTrace();
		}
	}
	
	/**
	 * Responds string content directly without encoding (JSON or MSGPACK) 
	 */
	@SuppressWarnings("resource")
	public static void respondString(HttpServletResponse response, String val){
		try{
			response.setContentType(CT_TEXT);
			if(val != null) {
				response.getWriter().write(val);
			}else {
				var content = std(Results.FAILURE);
				response.getWriter().write(content);
			}
			response.getWriter().flush();
		}catch(IOException e){ 
			e.printStackTrace();
		}
	}
	
	/**
	 * Responds binary content directly 
	 */
	@SuppressWarnings("resource")
	public static void respondBinary(HttpServletResponse response, byte[] val){
		try{
			response.setContentType(CT_TEXT);
			if(val != null) {
				response.getOutputStream().write(val);
			}else {
				var content = std(Results.FAILURE).getBytes();
				response.getOutputStream().write(content);
			}
			response.getOutputStream().flush();
		}catch(IOException e){ 
			e.printStackTrace();
		}
	}
	
	public<T> void respondsAsMessagePack(HttpServletResponse response, T result){
		supervise(response.getStatus());
		respondsMessagePack(response, result);
	}
	
	@SuppressWarnings("resource")
	public static<T> void respondsMessagePack(HttpServletResponse response, T result){
		try{ 
			response.setContentType(CT_BIN);
			Integer retval = errno(result);
			var rpcResult = std(retval, result);
			var content = Codec.bencode(rpcResult);
			response.getOutputStream().write(content);
			response.getOutputStream().flush();
		}catch(IOException e){ 
			e.printStackTrace();
		}
	}
	
	/**
	 * Without response content
	 */
	public static String std(Integer retval) {
		return std(retval, null);
	}
	
	//Binary
	public static<T> RpcResult<T> std(Integer retval, T result){
		var errno = retval != null ? retval : Integer.valueOf(0);
		var msg = getErrinfo().get(errno);
		return new RpcResult<T>(errno, msg, result);
	}
	
	//String
	protected static String std(Integer retval, String data) {
		var errno = retval != null ? retval : Integer.valueOf(0);
		var msg = getErrinfo().get(errno);
		return new StringBuilder(512)
					 .append("{\"code\":")
			 		 .append(errno.toString())
					 .append(",\"msg\":\"")
					 .append(msg)
					 .append("\",\"data\":")
					 .append(data)
					 .append("}").toString();
	}
	
	public static Integer errno(Object content) {
		if(content == null) return Integer.valueOf(-1); //Failure
		if(!(content instanceof Integer)) return Integer.valueOf(0);
		Integer result = (Integer)content;
		return result.intValue() <= 0 ? result : Integer.valueOf(0);
	}
	
	public int getUserAgent(HttpServletRequest request) {
		return TechartsServlet.getUserAgent(request);
	}
	
	public String getRemoteAddress(HttpServletRequest req) {
		return TechartsServlet.getRemorteAddress(req);
	}	
	
	/**
	 * Returns an integer represents the parameter of "userId".<br>
	 * It's a wrapper of i(request, "userId");
	 * */
	public int userId( HttpServletRequest req) {
		return i(req, PARAM_USERID);
	}
	
	public int owner( HttpServletRequest req) {
		return i(req, PARAM_OWNER);
	}
	
	public int id( HttpServletRequest req) {
		return i(req, PARAM_ID);
	}
	
	public String name(HttpServletRequest req) {
		return req.getParameter("name");
	}
	
	public int workflow(HttpServletRequest req) {
		return i(req, "workflow");
	}
	
	public int offset(HttpServletRequest req) {
		return i(req, "offset");
	}
	
	public int page(HttpServletRequest req) {
		return i(req, "offset");
	}
	
	public int status(HttpServletRequest req) {
		return i(req, "status");
	}
	
	public int type(HttpServletRequest req) {
		return i(req, "type");
	}
	
	/**Get Integer Parameter*/
	/**Get Integer Parameter*/
	public int i( HttpServletRequest req, String arg){
		//this._________(req, arg, "int");
		var p = Empty.trim(req.getParameter( arg));
		if( p == null) return 0;
		try{ return Integer.parseInt( p);
		}catch( NumberFormatException e){ return 0;}
	}
	
	public int i( HttpServletRequest req, String arg, int def){
		//this._________(req, arg, "int");
		var p = Empty.trim(req.getParameter( arg));
		if( p == null) return def;
		try{ return Integer.parseInt( p);
		}catch( NumberFormatException e){ return def;}
	}
	
	/**Get Double Parameter*/
	public double d( HttpServletRequest req, String arg){
		//this._________(req, arg, "double");
		var p = Empty.trim(req.getParameter( arg));
		if( p == null) return 0d;
		try{ return Double.parseDouble( p);
		}catch( NumberFormatException e){ return 0d;}
	}
	/**Get Float Parameter*/
	public float f( HttpServletRequest req, String arg){
		//this._________(req, arg, "float");
		var p = Empty.trim(req.getParameter( arg));
		if( p == null) return 0f;
		try{ return Float.parseFloat( p);
		}catch( NumberFormatException e){ return 0f;}
	}
	/**Get Boolean Parameter*/
	public boolean b( HttpServletRequest req, String arg){
		//this._________(req, arg, "bool");
		String p = Empty.trim(req.getParameter( arg));
		if( p == null) return false;
		if( "0".equals( p)) return false;
		if( "1".equals( p)) return true;
		return "true".equals(p) ? true :false;
	}
	
	/**Get Boolean Parameter*/
	public boolean b( HttpServletRequest req, String arg, boolean def){
		//this._________(req, arg, "bool");
		var p = Empty.trim(req.getParameter( arg));
		if(p == null) return def;
		if( "0".equals( p)) return false;
		if( "1".equals( p)) return true;
		return "true".equals(p) ? true :false;
	}
	
	/**Get String Parameter*/
	public String s( HttpServletRequest req, String arg){
		//this._________(req, arg, "string");
		var result = Empty.trim(req.getParameter(arg));
		if(result == null) return null;
		if("null".equalsIgnoreCase(result)) return null;
		return ("undefined".equalsIgnoreCase(result)) ? null : result;
	}
	
	public long l( HttpServletRequest req, String arg){
		//this._________(req, arg, "long");
		var p = Empty.trim(req.getParameter( arg));
		if( p == null) return 0L;
		try{ return Long.parseLong(p);
		}catch( NumberFormatException e){ return 0L;}
	}
	
	/**Get Date Parameter*/
	public Date t( HttpServletRequest req, String arg){
		//this._________(req, arg, "date");
		var p = Empty.trim(req.getParameter(arg));
		if(p == null) return null;
		int length = p.length();
		if(length < 10) return null;
		if(length == 16) p += ":00";
		if(length == 10) p += " 00:00:00";
		return Time.parse(p);
	}
	
	/**
	 * Write the API document into REDIS
	 */
	protected void _________(HttpServletRequest req, String param, String  type) {
		var name = TechartsServlet.getServiceApi(req);
		ServiceCache.writeApiDoc(name, param, type);
	}
}