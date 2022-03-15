package cn.com.techarts.msx.rpc;

import java.util.Set;

import org.apache.http.Consts;
import org.apache.http.entity.ContentType;

public final class HttpConst {
	public static final String HEADER_CONNECTION = "Connection"; 
	public static final int SC_OK = 200, SC_500 = 500, SC_UNKNOWN = -1;
	public static final int TO_CON = 5000, TO_REQ = 5000, TO_SOC = 10000;
	public static final ContentType CT_TEXT = ContentType.create("text/plain", Consts.UTF_8);
	public static final ContentType CT_JSON = ContentType.create("application/json;charset=UTF-8");
	public static final ContentType CT_FORM = ContentType.create("application/x-www-form-urlencoded;charset=UTF-8");
	
	
	//The error codes in the set are unrecoverable(business exceptions or other fatal errors)
	public static final Set<String> UNRECOVERABLES = Set.of("404", "500", "400", "403", "401");
	
	public static ContentType createContentType(String contentType) {
		if(contentType == null) return CT_FORM;
		return ContentType.create(contentType);
	}
}
