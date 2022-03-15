package cn.com.techarts.msx.cluster;

/**
 * If the request is failed, how do you want to do?
 * */
public enum FaultStrategy {
	Ignored,	//Discards the request directly	
	Retry,		//Requests again on the same server
	Failover,	//Requests n times on another alive servers (default 2 times)
	Forking2	//Requests 2 alive servers in the meantime and keeps the faster response
}
