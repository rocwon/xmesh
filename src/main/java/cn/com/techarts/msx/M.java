package cn.com.techarts.msx;

/**
 * HTTP METHOD<p>
 * Now it just supports Get and Post
 */
public enum M/*ethod**/ {
	GET("get"),
	POST("post");
	
	
	private String name;
	
	M(String name){
		this.setName(name);
	}
	
	public boolean isGet(String method) {
		if(method == null) return false;
		return "get".equalsIgnoreCase(method);
	}
	
	public boolean isPost(String method) {
		if(method == null) return false;
		return "post".equalsIgnoreCase(method);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public boolean match(String method) {
		if(name == null) return false;
		if(method == null) return false;
		return this.name.equalsIgnoreCase(method);
	}
}