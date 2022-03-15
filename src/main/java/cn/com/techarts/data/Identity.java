package cn.com.techarts.data;

public class Identity
{
	private int tid;
	
	private int id;
	
	/**
	 * The smallest Id. ZERO(0) is reserved for system.
	 * */
	public static final int ID_INITIAL = 1;
	
	/**
	 * INVALID_ID means the Id is ZERO(0) and it's <b>invalid</b>.
	 * In Relax, ZERO(0) is reserved for system. 
	 * */
	public static final int ERRID = 0;
	
	/** Table name */
	private String table; 
	
	public Identity(){}
	
	public Identity( int id, int tid)
	{
		setId( id);
		setTid( tid);
	}
	
	public Identity( int id, String table)
	{
		setId( id);
		setTable( table);
	}
	
	public Identity( int id, int tid, String table)
	{
		this( id, tid);
		setTable( table);
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public int getId() {
		return id;
	}
	
	/** Get the table name */
	public void setTable(String table) {
		this.table = table;
	}
	
	/** Set the table names */
	public String getTable() {
		return table;
	}

	public void setTid(int tid) {
		this.tid = tid;
	}

	public int getTid() {
		return tid;
	}
}