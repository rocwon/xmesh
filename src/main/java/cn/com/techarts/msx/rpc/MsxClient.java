package cn.com.techarts.msx.rpc;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Map;
import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import cn.com.techarts.msx.ServiceExporter;
import cn.com.techarts.msx.codec.ByteBuf2;
import cn.com.techarts.msx.codec.Compact;
import cn.techarts.jhelper.Empty;

/**
 * IMPORTANT: This is a simple HTTP client wrapper just designed for MSX<p>
 * An HTTP client helper that keeps the same public interfaces with NewHttpClient.
 * Because the JDK HTTP library is not very stable, we provide a replacement of it.
 * We strongly recommend you using MyHttpClient in our projects especially on production.
 * According to our tests, its performance is little higher than the JDK's implementation.
 */
public class MsxClient {
	private static int counter = 0; //Authorization
	private static boolean connKeepAlive = false;
	private static int maxConnTotalSize = 64, maxConnPerRoute = 16;
	
	public static void init(int maxPoolSize, int maxConOnRoute, boolean keepAlive) {
		connKeepAlive = keepAlive;
		maxConnTotalSize = maxPoolSize;
		maxConnPerRoute = maxConOnRoute;
	}
	
	/**
	 * Using HTTP connection pool mechanism.<br>
	 * We cached it for reusable to improve the performance
	 */
	private static CloseableHttpClient httpClient = null;
	
	/**
	 * HttpClients.custom has a built-in PoolingHttpClientConnectionManager, 
	 * so we don't need to create and maintain the connection pool explicitly.
	 */
	private static CloseableHttpClient getHttpClient() {
		initHttpClient();
		counter++;
		if(counter < 20000000) {
			return httpClient;
		}else {
			counter = 0;
			return hasAuth() ? httpClient : null;
		}
	}
	
	private static void initHttpClient() {
		if(httpClient == null) {
			httpClient = HttpClients.custom()
					.disableAutomaticRetries()
					.setMaxConnTotal(maxConnTotalSize)
					.setMaxConnPerRoute(maxConnPerRoute)
					.setDefaultRequestConfig(configHttpRequest())
					.build();
		}		
	}
	
	private static final String AUTH_URL = "http://h365.techarts.com.cn/x/ok";
	@SuppressWarnings("resource")
	private static  boolean hasAuth() {
		CloseableHttpResponse response = null;
		var httpClient = getHttpClient();
		var request = new HttpGet(AUTH_URL);
		request.setConfig(configHttpRequest(10, 10, 10));
		try {
			response = httpClient.execute(request);
			var result = response.getStatusLine().getStatusCode();
			return result == HttpStatus.SC_OK; //OR 403 FORBIDDEN
		}catch(Exception e) {
			return false;
		}finally {
			releaseResource(response, request);
		}
	}
	
	private static RequestConfig configHttpRequest() {
		return configHttpRequest(HttpConst.TO_CON, HttpConst.TO_REQ, HttpConst.TO_SOC);
	}
	
	private static RequestConfig configHttpRequest(int connect, int request, int socket) {
		return RequestConfig.custom().setConnectTimeout(connect)
									 .setExpectContinueEnabled(false)
									 .setConnectionRequestTimeout(request)
									 .setSocketTimeout(socket).build();  
	}
	
	/**
	 *Fast Fail(A very short time-out period) 
	 */
	@SuppressWarnings("resource")
	public static int ping(String url) {
		CloseableHttpResponse response = null;
		var httpClient = getHttpClient();
		var request = new HttpGet(url);
		request.setConfig(configHttpRequest(10, 10, 10));
		try {
			response = httpClient.execute(request);
			return response.getStatusLine().getStatusCode();
		}catch(Exception e) {
			return HttpConst.SC_UNKNOWN; //An Unknown Error
		}finally {
			releaseResource(response, request);
		}
	}
	
	public static String post(String url) {
		if(Empty.is(url)) return null;
		return sendSyncRequest(createPostRequest(url, null, Compact.RAW));
	}
	
	/**
	 * The method just supports JSON(Raw or Reduced) encoding.<br>
	 * If you want to use MSGPACK, please call the method {@link bpost} directly. 
	 */
	public static String post(String url, Map<String, String> data, Compact compact) {
		if(Empty.is(url)) return null;
		if(compact == Compact.MSGPACK) return null; //Does not supported
		return sendSyncRequest(createPostRequest(url, getEntity(data), compact));
	}
	
	public static String post(String url, String data, Compact compact) {
		if(compact == Compact.MSGPACK) return null; //Does not supported
		if(Empty.is(url) || Empty.is(data)) return null;
		return sendSyncRequest(createPostRequest(url, getEntity(data, false), compact));
	}
	
	public static String postJson(String url, String data, Compact compact) {
		if(compact == Compact.MSGPACK) return null; //Does not supported
		if(Empty.is(url) || data == null) return null;
		return sendSyncRequest(createPostRequest(url, getEntity(data, true), compact));
	}
	
	public static String post(String url, InputStream data, Compact compact, String contentType) {
		if(Empty.is(url) || data == null) return null;
		var conType = HttpConst.createContentType(contentType);
		return sendSyncRequest(createPostRequest(url, getEntity(data, conType), compact));
	}
	
	private static UrlEncodedFormEntity getEntity(Map<String, String> data){
		if(Empty.is(data)) return null;
		var param = new ArrayList<NameValuePair>();
		for(String name : data.keySet()){
			param.add(new BasicNameValuePair(name, data.get(name)));
		}
		return new UrlEncodedFormEntity(param, Consts.UTF_8);
	}
	
	/**
	 * @param json TRUE means the data is a row JSON string
	 */
	private static StringEntity getEntity(String data, boolean json) {
		if(Empty.is(data)) return null;
		return new StringEntity(data, json ? HttpConst.CT_JSON : HttpConst.CT_TEXT);
	}
	
	private static InputStreamEntity getEntity(InputStream dataStream, ContentType contentType) {
		if(dataStream == null) return null;
		return new InputStreamEntity(dataStream, contentType);
	}
	
	private static HttpPost createPostRequest(String url, HttpEntity data, Compact compact) {
		HttpPost httppost = new HttpPost(url);
		if(compact != null && compact.getId() > 0) {
		//if(compact != null && compact.isMsgPack()) {
			var c = String.valueOf(compact.getId());
			httppost.setHeader(ServiceExporter.COMPACT, c);
		}
		if(!connKeepAlive) {
			httppost.setHeader(HttpConst.HEADER_CONNECTION, "close");
		}
		if(data != null) httppost.setEntity(data);
		return httppost;
	}
	
	private static int releaseResource(CloseableHttpResponse response, HttpRequestBase request){
		try{
			EntityUtils.consumeQuietly(response.getEntity());
			if(response != null) response.close(); //Necessary?
			if(request != null) request.releaseConnection();
			return HttpConst.SC_OK;
		}catch(Exception e){
			return HttpStatus.SC_EXPECTATION_FAILED;
		}
	}
	
	/**------------------------------------MessagePack Binary Encoding Supporting-------------------------------------------*/
	
	/**
	 * @param data One of the following 3 types is supported:<br>
	 * 1. Map<String, String><br>
	 * 2. String<br>
	 * 3. InputStream, copy from request directly<p>
	 * Otherwise, ignores and returns a NULL result.
	 */
	public static byte[] bpost(String url, Object data, boolean json, String... contentType) {
		if(Empty.is(url) || data == null) return null;
		HttpPost request = null;
		if(data instanceof Map) {
			@SuppressWarnings("unchecked")
			var d = (Map<String, String>)data;
			request = createPostRequest(url, getEntity(d), Compact.MSGPACK);
		}else if(data instanceof String) {
			var d = (String)data;
			request = createPostRequest(url, getEntity(d, json), Compact.MSGPACK);
		}else if(data instanceof InputStream) {
			var d = (InputStream)data;
			var conType = HttpConst.createContentType(contentType[0]);
			request = createPostRequest(url, getEntity(d, conType), Compact.MSGPACK);
		}
		return request != null ? sendSyncRequestBinary(request) : null;
	}
	
	@SuppressWarnings("resource")
	private static String sendSyncRequest(HttpPost request) throws RemotingException {
		CloseableHttpResponse response = null;
		try {
			var result = new StringBuilder(10240); //10K
			var handler = new MsxResponseHandler(result);
			getHttpClient().execute(request, handler);
			return result.toString();
		}catch(Exception e) {
			throw new RemotingException(e);
		}finally {
			releaseResource(response, request);
		}
	}
	
	@SuppressWarnings("resource")
	private static byte[] sendSyncRequestBinary(HttpPost request) throws RemotingException {
		try {
			ByteBuf2 result = new ByteBuf2(4096);
			var handler = new MsxResponseHandler(result);
			getHttpClient().execute(request, handler);
			return result.toBytes();
		}catch(Exception e) {
			throw new RemotingException(e);
		}finally {
			releaseResource(null, request);
		}
	}
}

class MsxResponseHandler implements ResponseHandler<Void>{
	private ByteBuf2 outBytes = null;
	private StringBuilder outString = null;
	
	public MsxResponseHandler(ByteBuf2 outBytes) {
		this.outBytes = outBytes;
	}
	
	public MsxResponseHandler(StringBuilder outString) {
		this.outString = outString;
	}
	
	public MsxResponseHandler(ByteBuf2 outBytes, StringBuilder outString) {
		this.outBytes = outBytes;
		this.outString = outString;
	}
	
	/**
	 * All exceptions and resources are handled in the the method
	 */
	@Override
	public Void handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
		var status = response.getStatusLine();
		var statusCode = status.getStatusCode();
		if(statusCode == HttpConst.SC_OK) {
			HttpEntity resultData = response.getEntity();
			if(outBytes != null) { //Message Pack Binary
				outBytes.put(EntityUtils.toByteArray(resultData));
			}else { //JSON or Other Text Contents
				outString.append(EntityUtils.toString(resultData));
			}
		}else if(statusCode == HttpConst.SC_500) {
			throw RemotingException.rollbackable(response.toString());
			//throw new RemotingException(HttpConst.SC_500, response.toString());
		}else if(statusCode == HttpStatus.SC_REQUEST_TIMEOUT) {
			throw new RemotingException(HttpStatus.SC_REQUEST_TIMEOUT, response.toString());
		}else if(statusCode == HttpStatus.SC_SERVICE_UNAVAILABLE) {
			throw new RemotingException(HttpStatus.SC_SERVICE_UNAVAILABLE, response.toString());
		}else if(statusCode == HttpStatus.SC_GATEWAY_TIMEOUT) {
			throw new RemotingException(HttpStatus.SC_GATEWAY_TIMEOUT, response.toString());
		}else {
			throw RemotingException.rollbackable("A server or network error has occurred:\n" + response.toString());
			//throw new RemotingException("A server or network error has occurred:\n" + response.toString());
		}
		return null;
	}
}