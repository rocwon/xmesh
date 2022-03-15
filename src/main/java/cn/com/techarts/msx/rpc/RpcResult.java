package cn.com.techarts.msx.rpc;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.core.type.TypeReference;

import cn.com.techarts.basic.Results;
import cn.com.techarts.msx.codec.Codec;

public final class RpcResult<T> {
	private int code;
	private String msg;
	private T data;
	
	public RpcResult() {}
	
	public RpcResult(int code, String msg, T data) {
		this.code = code;
		this.msg = msg;
		this.data = data;
	}
	
	public int getCode() {
		return code;
	}
	public void setCode(int code) {
		this.code = code;
	}
	public String getMsg() {
		return msg;
	}
	public void setMsg(String msg) {
		this.msg = msg;
	}
	public T getData() {
		return data;
	}
	public void setData(T data) {
		this.data = data;
	}
	
	/**
	 * Throws the BusinessException if code is less than 0
	 */
	public boolean failed() {
//		if(code < Results.SUCCESS) {
//			throw new BusinessException(code, msg);
//		}
//		return false;
		return code < Results.SUCCESS; //The simple implementation
	}
	
	//------------------Data Converters-------------------------
	
	/**
	 * If a business exception is occurred, the method 
	 * returns the error code directly(a negative number) 
	 */
	public int toInt() {
		if(failed()) return code;
		var result = Codec.convert(data, Integer.class);
		return result != null ? result.intValue() : 0;
	}
	
	public long toLong() {
		if(failed()) return 0;
		var result = Codec.convert(data, Long.class);
		return result != null ? result.longValue() : 0l;
	}
	
	public float toFloat() {
		if(failed()) return 0f;
		var result = Codec.convert(data, Float.class);
		return result != null ? result.floatValue() : 0f;
	}
	
	public double toDbl() {
		if(failed()) return 0f;
		var result = Codec.convert(data, Double.class);
		return result != null ? result.doubleValue() : 0d;
	}
	
	public String toStr() {
		if(failed()) return null;
		return Codec.convert(data, String.class);
	}
	
	public<E> E toObj(Class<E> t) {
		if(failed()) return null;
		return Codec.convert(data, t);
	}
	
	public <E> List<E> toList(TypeReference<List<E>> type) {
		if(failed() || type == null) return null;
		return Codec.convert2List(data, type);
	}
	
	public<E> Set<E> toSet(TypeReference<Set<E>> type) {
		if(failed()) return null;
		return Codec.convert2Set(data, type);
	}
	
	public List<String> toStrList() {
		if(failed()) return null;
		var type = new TypeReference<List<String>>() {};
		return Codec.convert2List(data, type);
	}
	
	public List<Integer> toIntList() {
		if(failed()) return null;
		var type = new TypeReference<List<Integer>>() {};
		return Codec.convert2List(data, type);
	}
	
	public Map<String, String> toMap() {
		if(failed()) return null;
		var type = new TypeReference<Map<String, String>>() {};
		return Codec.convert2Map(data, type);
	}
	
	public <V> Map<Integer, V> toIntMap(TypeReference<Map<Integer, V>> type) {
		if(failed() || type == null) return null;
		return Codec.convert2Map(data, type);
	}
	
	public <V> Map<String, V> toStrMap(TypeReference<Map<String, V>> type) {
		if(failed() || type == null) return null;
		return Codec.convert2Map(data, type);
	}
	
	public <E> E[] toArray(TypeReference<E[]> type) {
		if(failed() || type == null) return null;
		return Codec.convert2Array(data, type);
	}
	
	public int[] toIntArray() {
		if(failed()) return null;
		return Codec.convert2IntArray(data);
	}
}