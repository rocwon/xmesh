package cn.com.techarts.msx.server;

import java.util.Map;
import java.util.HashMap;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpObject;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.multipart.MemoryAttribute;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import io.netty.handler.codec.http.multipart.DefaultHttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;


public class HttpServiceHandler extends SimpleChannelInboundHandler<HttpObject>{
	
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, HttpObject msg) throws Exception {
		var request = (FullHttpRequest)msg;
		var params = parseContent(request);
		var service = getService(request.uri());
		System.out.println(service);
		var handler = LocalCache.getHandler(service);
		if(handler == null) return; 
		var response = handler.handle(params);
		if(response != null) {
			var bytes = response.getBytes();
			var r = RestfulHandler.makeResponse(bytes);
			if(r != null)ctx.channel().writeAndFlush(r);
		}
	}
	
	private String getService(String uri) {
		int index = uri.indexOf('?');
		if(index <= 0) return uri;
		return uri.substring(0, index);
	}
	
	public Map<String, String> parseContent(FullHttpRequest request) {
        var method = request.method();
        Map<String, String> result = new HashMap<>();
        if(HttpMethod.POST == method) {
        	return getParameters(request);
        }
        if (HttpMethod.GET != method) {
        	return Map.of(); //These methods are not supported
        }
        var decoder = new QueryStringDecoder(request.uri());
        decoder.parameters().entrySet().forEach(entry->{
        	var values = entry.getValue();
        	if(values != null && !values.isEmpty()) {
        		result.put(entry.getKey(), values.get(0));
        	}
        });
        return result;
    }
	
	private Map<String, String> getParameters(FullHttpRequest request) {
		var factory = new DefaultHttpDataFactory(false);
	    var decoder = new HttpPostRequestDecoder(factory, request);
	    var httpPostData = decoder.getBodyHttpDatas();
	    Map<String, String> result = new HashMap<>();
	    var supported = InterfaceHttpData.HttpDataType.Attribute;
	    for (InterfaceHttpData data : httpPostData) {
	        if (data.getHttpDataType() == supported) {
	            var attribute = (MemoryAttribute) data;
	            result.put(attribute.getName(), attribute.getValue());
	        }
	    }
	    return result;
	}
}