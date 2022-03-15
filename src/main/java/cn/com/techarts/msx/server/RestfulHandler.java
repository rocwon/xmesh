package cn.com.techarts.msx.server;

import java.util.Map;
import cn.com.techarts.data.SimpleDaoHelper;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;

public abstract class RestfulHandler {
	private String service = null;
	private SimpleDaoHelper persister = null;
	
	private static final String CT_JSON = "application/json;charset=UTF-8";
	
	public abstract String handle(Map<String, String> params);
	
	public String getService() {
		return service;
	}
	
	public void setService(String service) {
		this.service = service;
	}

	public SimpleDaoHelper getPersister() {
		return persister;
	}

	public void setPersister(SimpleDaoHelper persister) {
		this.persister = persister;
	}
	
	protected String sql(String name) {
		return LocalCache.getStatement(name);
	}
	
	public static FullHttpResponse makeResponse(byte[] content) {
		 var response = new DefaultFullHttpResponse(
				 			HttpVersion.HTTP_1_1,
                			HttpResponseStatus.OK, 
                			Unpooled.wrappedBuffer(content));
		 response.headers().set("Content-Type", CT_JSON);
		 var contentLength = response.content().readableBytes();
		 response.headers().set("Content-Length", contentLength);
		return response; 
	}
}
