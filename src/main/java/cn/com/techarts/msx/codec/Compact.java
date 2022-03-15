package cn.com.techarts.msx.codec;

public enum Compact {
	
	/**
	 * Raw JSON encoding all properties(including number 0 and NULL) of the object.
	 * Now we output the full JSON for all APIs over HTTP because of front-ends issue.
	 * But it's not a best way, too many bandwidth wasted by those meaningless bytes.<br>
	 * It's based on FasterJSON(JACKSON)
	 */
	RAW(0),
	
	/**
	 * Supports binary encoding over MSGPACK specification to improve the performance.
	 * According to the benchmark tests, it saves about 30% network traffic at least.
	 */
	MSGPACK(1),
	/**
	 * Ignores all default values such as number 0 or NULL to reduce the size of content.
	 * REDIS object serialization and the XMESH RPC use these compact JSON encoding.<br>
	 * It's based on FasterJSON(JACKSON)
	 */
	REDUCED(2);
	
	private Compact(int id) {
		this.setId(id);
	}
	
	private int id = 0;
	
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public boolean isMsgPack() {
		return this.getId() == MSGPACK.id;
	}
	
	public static final int RAW_JSON_STRING = 0;
	public static final int BINARY_MSG_PACK = 1;
	public static final int IGNORE_DEF_VALS = 2;
}
