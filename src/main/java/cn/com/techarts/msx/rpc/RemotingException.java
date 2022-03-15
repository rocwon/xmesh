package cn.com.techarts.msx.rpc;

import cn.com.techarts.data.BasicDaoException;

/**
 * A runtime exception that indicates a network or business logic error has occurred.<p>
 * Some of these exceptions are unrecoverable, it will be threw to the client ignoring fault-strategy.
 */
public class RemotingException extends RuntimeException {
	private int httpStatusCode = 200;
	
	public RemotingException(String cause) {
		super(cause);
	}
	
	public RemotingException(int statusCode) {
		this.setHttpStatusCode(statusCode);
	}
	
	public RemotingException(Exception e) {
		super(e.getMessage());
		if(e instanceof RemotingException) {
			var f = (RemotingException)e;
			httpStatusCode = f.getHttpStatusCode();
		}
	}
	
	public RemotingException(int statusCode, String cause) {
		super(cause);
		setHttpStatusCode(statusCode);
	}
	
	public static BasicDaoException rollbackable(String cause) {
		return new BasicDaoException(cause);
	}
	
	/**
	 * Without available service node 
	 */
	public static RemotingException serviceless() {
		var result = new RemotingException("\n#--- Fatal Error: No available service node---#\n");
		return result;
	}

	public int getHttpStatusCode() {
		return httpStatusCode;
	}

	public void setHttpStatusCode(int httpStatusCode) {
		this.httpStatusCode = httpStatusCode;
	}
	
	public boolean isBusinessLogicException() {
		return this.httpStatusCode == 500;
	}
	
	public boolean isFatalServiceException() {
		switch(httpStatusCode) {
		case -1212:	//REMOTING FAULT
		case 408:	//REQUEST TIMEOUT
		case 503:	//SERVICE UNAVAILABLE
		case 504:	//GATEWAY TIMEOUT
			return false;
		default:
			return true;
		}
	}
}