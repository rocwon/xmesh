package cn.com.techarts.msx.rpc;

import cn.com.techarts.data.BasicDaoException;

public class BusinessException extends BasicDaoException{
	private int code; //Error Code

	public BusinessException(int code, String cause) {
		super(cause);
		this.code = code;
	}
	
	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}
	
}
