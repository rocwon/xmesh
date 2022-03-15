package cn.com.techarts.msx;

public interface SecurityManager {
	public static final int ALLOWED = 0; //OK
	public static final int DENIED = -10020;
	public static final int INVALID_SESSION = -10000;
	
	/**
	 * You must implement your own session strategy<br>
	 * @return One of ALLOWED, DENIED OR INVALIDE_SESSION 
	 */
	default public int checkSession(int user, String ip, int agent, String session) {
		return ALLOWED;
	}
	
	/**
	 * An API is allowed to access escaping permission checking
	 */
	public abstract boolean isAllowedAlways(String api);
	
	/**
	 * Detects the client IP address is whether in the white-list
	 */
	public abstract boolean isClientAllowed(String ip, String api);
}