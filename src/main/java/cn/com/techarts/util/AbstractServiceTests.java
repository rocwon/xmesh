package cn.com.techarts.util;

import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;

import cn.com.techarts.data.DaoHelper;

/**
 * It only can be used in Spring Framework.
 * */
@ContextConfiguration(locations= {"file:D:/Studio/Project/Java/blade/src/main/webapp/WEB-INF/testApplicationContext.xml"})
public class AbstractServiceTests extends AbstractTransactionalJUnit4SpringContextTests
{
	public Object getBean( String id)
	{
		if(applicationContext == null) return null;
		return this.applicationContext.getBean( id);
	}
	
	public<T> T getBean( String id, Class<T> t)
	{
		if(applicationContext == null) return null;
		return t.cast( applicationContext.getBean( id));
	}
	
	public DaoHelper getPersister()
	{
		if(applicationContext == null) return null;
		return (DaoHelper)this.applicationContext.getBean( "daoHelper");
	}
	
	public void echo( Object info)
	{
		System.out.println( "\n\n############: " + info + "\n\n");
	}
}
