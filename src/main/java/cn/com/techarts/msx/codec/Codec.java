package cn.com.techarts.msx.codec;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import cn.com.techarts.msx.rpc.RpcResult;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.core.type.TypeReference;
import org.msgpack.jackson.dataformat.MessagePackFactory;

/**
 * The utility is based on Faster JSON.<br>
 * It supports RAW-JSON, COMPACT-JSON and MSGPACK<p>
 * To improve performance, we cached(static fields) the encoder/decoder to reuse.
 * According to our tests, it's faster than FAST-JSON and lower memory usages and without concurrent issue.
 */
public class Codec {
	private static ObjectMapper jcodec = null,	//RAW JSON CODEC
								bcodec = null, 	//MSGPACK CODEC
								ccodec = null;	//COMPACT JSON CODEC
	
	static {
		jcodec = new ObjectMapper(); //Normal JSON string(with all properties)(Raw JSON)
		ccodec = new ObjectMapper(); //Serialize to JSON but ignore default values(Compact JSON)
		bcodec = new ObjectMapper(new MessagePackFactory());//Serialize to BINARY bytes(MSGPACK)
		ccodec.setSerializationInclusion(Include.NON_DEFAULT);
		jcodec.configure(Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
		ccodec.configure(Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
		bcodec.configure(Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
		jcodec.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		ccodec.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		bcodec.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	}
	
	/**
	 * Encode to JSON
	 */
	public static String encode(Object src)  throws RuntimeException{
		try{
			return jcodec.writeValueAsString( src);
		}catch( Exception e){
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Encode to JSON
	 * @param jgnoreDefaultValue Ignored all properties which value is NULL or 0. 
	 */
	public static String encode(Object src, boolean compact)  throws RuntimeException{
		try{
			if(compact) {
				return ccodec.writeValueAsString(src);
			}else {
				return jcodec.writeValueAsString(src);
			}
		}catch( Exception e){
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Encode to BINARY (MessagePack)
	 */
	public static byte[] bencode(Object src) throws RuntimeException {
		try{
			return bcodec.writeValueAsBytes( src);
		}catch( Exception e){
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Decode from JSON
	 */
	public static<T> T decode(String src, Class<T> targetClass) throws RuntimeException {
		try {
			return jcodec.readValue(src, targetClass);
		}catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Decode from BINARY (MessagePack)
	 */
	public static<T> T decode(byte[] src, Class<T> targetClass) throws RuntimeException {
		try {
			return bcodec.readValue(src, targetClass);
		}catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Decode from JSON result
	 */
	public static RpcResult<Object> decodeRpcResult(String result) throws RuntimeException{
		try {
			var type = new TypeReference<RpcResult<Object>>(){};
			return jcodec.readValue(result, type);
		}catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Decode from BINARY result (MessagePack)
	 */
	public static RpcResult<Object> decodeRpcResult(byte[] result) throws RuntimeException{
		try {
			var type = new TypeReference<RpcResult<Object>>(){};
			return bcodec.readValue(result, type);
		}catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * You MUST call one of the series methods to convert the RpcResult.data to the specific type you want
	 * 
	 */
	public static<T> T convert(Object source, Class<T> targetClass){
		if(source == null) return null;
		try {
			return jcodec.convertValue(source, targetClass);
		}catch(Exception e) {
			return null;
		}
	}
	
	/**
	 * You MUST call one of the series methods to convert the RpcResult.data to the specific type you want
	 * 
	 */
	public static<T> T[] convert2Array(Object source, TypeReference<T[]> type){
		if(source == null) return null;
		try {
			return jcodec.convertValue(source, type);
		}catch(Exception e) {
			return null;
		}
	}
	
	/**
	 * You MUST call one of the series methods to convert the RpcResult.data to the specific type you want
	 * 
	 */
	public static int[] convert2IntArray(Object source){
		if(source == null) return null;
		try {
			var type = new TypeReference<int[]>() {};
			return jcodec.convertValue(source, type);
		}catch(Exception e) {
			return null;
		}
	}
	
	/**
	 * You MUST call one of the series methods to convert the RpcResult.data to the specific type you want
	 * 
	 */
	public static<T> List<T> convert2List(Object source, TypeReference<List<T>> type){
		if(source == null) return null;
		try {
			return jcodec.convertValue(source, type);
		}catch(Exception e) {
			return null;
		}
	}
	
	/**
	 * You MUST call one of the series methods to convert the RpcResult.data to the specific type you want
	 * 
	 */
	public static<T> Set<T> convert2Set(Object source, TypeReference<Set<T>> type){
		if(source == null) return null;
		try {
			return jcodec.convertValue(source, type);
		}catch(Exception e) {
			return null;
		}
	}
	
	/**
	 * You MUST call one of the series methods to convert the RpcResult.data to the specific type you want
	 * 
	 */
	public static<K, V> Map<K, V> convert2Map(Object source, TypeReference<Map<K, V>> type){
		if(source == null) return null;
		try {
			return jcodec.convertValue(source, type);
		}catch(Exception e) {
			return null;
		}
	}
}