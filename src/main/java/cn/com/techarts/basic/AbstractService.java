package cn.com.techarts.basic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

import cn.com.techarts.data.BasicDaoException;
import cn.com.techarts.data.DaoHelper;
import cn.com.techarts.data.Identity;
import cn.techarts.jhelper.Converter;
import cn.techarts.jhelper.Empty;
import cn.techarts.jhelper.Executor;

public abstract class AbstractService 
{
	protected DaoHelper persister = null;
	/**
	 * ERRID means the Id is ZERO(<b>0</b>) and it's <b>invalid</b>.
	 * In TECHARTS, ZERO(0) is reserved for system. 
	 * */
	public static final int ERRID = Identity.ERRID;
	public static final double ZERO = 0.00001D;
	
	public DaoHelper getPersister()
	{
		return persister;
	}
	
	public void setPersister( DaoHelper persister)
	{
		this.persister = persister;
	}
	
	/**
	 * Generates an Empty List, the corresponding method name is getEmptyList
	 * */
	public  <T> List<T> gel( Class<T> t, int... size)
	{
		int initial = 32;
		if(size != null && size.length == 1) {
			initial = size[0];
		}
		//return new FastList<T>(t);
		return new ArrayList<T>(initial);
	}
	
	/**
	 * gel means Get Empty Map, the corresponding method name is getEmptyMap
	 * */
	public<K,V> Map<K, V> gem(Class<K> k, Class<V> v, int... size){
		int initial = 32;
		if(size != null && size.length == 1) {
			initial = size[0];
		}
		return new HashMap<K,V>(initial);
	}
	
	public boolean checkId( int id)
	{
		return id < Identity.ID_INITIAL ? false : true;
	}
	
	public boolean checkVarArg( int[] objects)
	{
		return objects != null && objects.length > 0 ? true : false; 
	}
	
	public DaoHelper exec()
	{
		var result = getPersister();
		if( result != null) return result;
		throw new BasicDaoException( "Can't get the DaoHelper in the context. Please check your configuration.");
	} 
	
	public int s2i(String arg, int defval) {
		return Converter.toInt(arg, defval);
	}

	public static boolean isEmpty( Collection<?> param)
	{
		return param == null || param.isEmpty();
	}
	
	public static boolean isEmpty(IdObject obj) {
		return obj == null || obj.getId() == ERRID;
	}
	
	public static boolean isEmpty(String src) {
		return Empty.is(src);
	}
	
	public static boolean isEmpty(Map<?, ?> param) {
		return param == null || param.isEmpty();
	}
	
	public<T> int get(Map<T,Integer> map, T key){
		Integer result = map.get( key);
		return result != null ? result : 0;
	}
	
	public String uuid(){
		return UUID.randomUUID().toString();
	}
	
	public static boolean isTrue(int arg) {
		return arg == 0 ? false : true;
	}
	
	public boolean isTrue(String arg) {
		if(arg == null) return false;
		if("".equals(arg)) return false;
		return "0".equals(arg) ? false : true;
	}
	
	protected static<T> void async(Supplier<T> supplier) {
		Executor.execute(supplier);
	}

	protected static<T> void async(Supplier<T> supplier, boolean force) {
		var enabled = Executor.isAsyncMode();
		if(!enabled) {
			Executor.enableAsyncMode();
		}
		Executor.execute(supplier);
		if(!enabled) {
			Executor.disableAsyncMode();
		}		
	}
	
	/**
	 * Returns false if the parameter is NULL or owner equals 0
	 */
	protected <T extends UniqueObject>boolean validOwner(T arg){
		return arg == null || arg.getOwner() == ERRID ? false : true;
	}
	
	/**
	 * Returns false if the parameter is NULL or id equals 0
	 */
	protected <T extends IdObject> boolean valid(T arg) {
		return arg == null || arg.getId() == ERRID ? false : true;
	}
	
	public static final int SUCCESS = Results.Success.getId();
	public static final int FAILURE = Results.Failure.getId();	
}