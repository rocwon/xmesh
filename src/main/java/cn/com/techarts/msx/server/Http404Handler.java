package cn.com.techarts.msx.server;

import java.util.Map;

public class Http404Handler extends RestfulHandler {

	@Override
	public String handle(Map<String, String> params) {
		return "{\"code\":-10086,\"msg\":\"Can't find the service\"}";
	}

}
