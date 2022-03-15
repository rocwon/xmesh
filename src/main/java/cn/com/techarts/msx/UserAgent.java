package cn.com.techarts.msx;

public enum UserAgent {
	UNKNOWN(0),
	ANDROID(1),
	IOS(2),
	WEB(3),
	MOBILE(4),
	WEIXIN(5);
	
	private int id;
	
	private UserAgent(int id) {
		this.setId(id);
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	
	public static boolean isMobile(int agent) {
		if(agent == IOS.id) return true;
		return agent == ANDROID.id;
	}
}
