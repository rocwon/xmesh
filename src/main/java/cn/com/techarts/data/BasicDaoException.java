
package cn.com.techarts.data;

import org.springframework.dao.DataAccessException;

public class BasicDaoException extends DataAccessException
{	
	public BasicDaoException( String cause)
	{
		super( cause);
		
	}
	
	public BasicDaoException( String cause, Throwable e)
	{
		super( cause, e);
	}
	
	public BasicDaoException( int errno, String cause, Throwable e)
	{
		super( cause, e);
	}
}
