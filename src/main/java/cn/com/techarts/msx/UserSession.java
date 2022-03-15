package cn.com.techarts.msx;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

import cn.techarts.jhelper.Cryptor;
import cn.techarts.jhelper.Empty;
import cn.techarts.jhelper.Generator;
import cn.techarts.jhelper.Spliter;
import cn.techarts.jhelper.Time;

import java.util.HashMap;

public class UserSession implements Serializable {
	public static int DURATION = 0;
	private static String KEY = null;
	private static String SALT = null;
	public static boolean CACHEABLE = false;
	
	private Map<String, String> sessions;
	
	public static void init(String salt, int duration, String key, boolean cacheable) {
		KEY = key;
		SALT = salt;
		DURATION = duration;
		CACHEABLE = cacheable;
	}
	
	public UserSession(){
		this.sessions = new HashMap<>(4);
	}
	
	public UserSession(int agent, String session){
		sessions = new HashMap<>(4);
		this.appendNewDevice(session, agent);
	}
	
	public UserSession(int agent, String ip, String session){
		sessions = new HashMap<>(4);
		this.appendNewDevice(ip, session, agent);
	}
	
	public void appendNewDevice(String session, int agent) {
		var sessionInfo = new StringBuilder(session)
		   .append(',').append(new Date().getTime());
		if(sessions == null) sessions =  new HashMap<>(4);
		String key = String.valueOf(agent);
		if(sessions.containsKey(key)) sessions.remove(key);
		this.sessions.put(key, sessionInfo.toString());
	}
	
	public void appendNewDevice(String ip, String session, int agent) {
		var sessionInfo = new StringBuilder(session)
							 .append(',').append(ip);
		if(sessions == null) sessions =  new HashMap<>(4);
		String key = String.valueOf(agent);
		if(sessions.containsKey(key)) sessions.remove(key);
		this.sessions.put(key, sessionInfo.toString());
	}
	
	public boolean verify(int agent, String ip, String session) {
		if(!CACHEABLE) return false;
		var key = String.valueOf(agent);
		var sessionInfo = sessions.get(key);
		if(sessionInfo == null) return false;
		var fs = Spliter.split2Parts(sessionInfo, ',');
		if(fs == null || fs.length != 2) return false;
		return session.equals(fs[0]) && ip.equals(fs[1]);
	}
	
	public static boolean verify(String ip, int ua, int userId, String session) {
		if(CACHEABLE) return false;
		var tmp = Cryptor.decrypt(session, Cryptor.toBytes(KEY));
		if(Empty.is(tmp)) return false; //An invalid session
		var bgn = Integer.parseInt(tmp.substring(0, 8));
		if(Time.minutes() - bgn > DURATION) return false;
		var result = (ip != null ? ip : "0000") + ua + userId + SALT;
		return result != null ? result.equals(tmp.substring(8)) : false;
	}
	
	public void setSessions(Map<String, String> sessions) {
		this.sessions = sessions;
	}
	
	public Map<String, String> getSessions(){
		if(this.sessions == null) {
			this.sessions =  new HashMap<>(4);
		}
		return this.sessions;
	}
	
	public static UserAgent getUserAgentType(String agent) {
		if(agent == null) return UserAgent.UNKNOWN;
		if(agent.contains("MicroMessenger")) return UserAgent.WEIXIN;
		if(agent.contains("IOS")) return UserAgent.IOS;
		if(agent.contains("Android")) return UserAgent.ANDROID;
		return UserAgent.WEB; //All kinds of web browser kernels
	}
	
	private static String generateWithKey(String ip, int ua, int userId) {
		var result = (ip != null ? ip : "0000") + ua + userId;
		var minutes = String.valueOf(Time.minutes());
		result = minutes.concat(result).concat(SALT);
		return Cryptor.encrypt(result, Cryptor.toBytes(KEY));
	}
	
	public static String generate(String ip, int ua, int userId) {
		if(!CACHEABLE) {
			return generateWithKey(ip, ua, userId);
		}else {
			var result = Generator.uuid(); //A random string
			ServiceCache.cacheSession(userId, ip, ua, result);
			return result;
		}
	}
}