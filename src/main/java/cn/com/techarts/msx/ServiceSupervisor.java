package cn.com.techarts.msx;

public class ServiceSupervisor {
	private long bgn;
	private int broken;
	private int degrade;
	private int requests;
	
	public static final int BREAK = -1;
	public static final int DISCARD = 0;
	public static final int PROCESS = 1;
	
	public static final int ERRNO_BREAK = -10040;
	public static final int ERRNO_DISCARD = -10030;

	public static final String CACHE_KEY = "serviceSupervisor";
	/**
	 * The times of timeout occurred
	 * */
	private int failures;
	
	public int getBroken() {
		return broken;
	}
	public void setBroken(int broken) {
		this.broken = broken;
	}
	public int getDegrade() {
		return degrade;
	}
	public void setDegrade(int degrade) {
		this.degrade = degrade;
	}
	public long getBgn() {
		return bgn;
	}
	public void setBgn(long bgn) {
		this.bgn = bgn;
	}
	public int getFailures() {
		return failures;
	}
	public void setFailures(int failures) {
		this.failures = failures;
	}
	public int getRequests() {
		return requests;
	}
	public void setRequests(int requests) {
		this.requests = requests;
	}
	
	public ServiceSupervisor income() {
		this.requests++;
		return this;
	}
	public void failed() {
		this.failures += 1;
	}
	
	public int limits() {
		if(degrade > 0 && broken > 0) {
			return limitsByRequests();
		}else {
			return limitsByFailures();
		}
	}
	
	private int limitsByRequests() {
		var now = System.currentTimeMillis();
		if(now - bgn > 10000) { //10 seconds
			bgn = now;
			requests = 1;
			failures = 0;
			return PROCESS;
		}
		if(requests > broken) return BREAK;
		if(requests < degrade) return PROCESS;
		var diff = this.broken - this.degrade;
		var over = this.requests - this.degrade;
		var ratio = diff == 0f ? 0 : over / (float)diff;
		var num = ratio < 0.3 ? 3 : ratio > 0.7 ? 10 : 5;
		return requests % num == 0 ? PROCESS : DISCARD;
	}
	
	/**
	 * Failure Rate-------Income Request
	 * Less than 2%       Process All
	 * Less than 5%       Process 1 of 2
	 * Less than 10%      Process 1 of 5     
	 * Less than 20%      Process 1 of 10
	 * Great than 20%     Broken All
	 */
	private int limitsByFailures() {
		var now = System.currentTimeMillis();
		if(now - bgn > 10000) { //10 seconds
			bgn = now;
			requests = 1;
			failures = 0;
			return PROCESS;
		}
		if(requests == 0) return PROCESS;
		if(failures == 0) return PROCESS;
		var ratio = failures / (float)requests;
		if(ratio < 0.02f) return PROCESS; // 2% is accepted
		if(ratio >= 0.20f) return BREAK;  // Service Breaking 
		var num = ratio < 0.05 ? 2 : ratio > 0.10 ? 10 : 5;
		return requests % num == 0 ? PROCESS : DISCARD;
	}
}