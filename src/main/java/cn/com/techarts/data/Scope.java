package cn.com.techarts.data;

public final class Scope<T> {
	private int id;
	private T lower;
	private T upper;
	
	private int page;
	
	public Scope(){}
	
	public Scope( int id, T lower, T upper, int page){
		this.id = id;
		this.lower = lower;
		this.upper = upper;
		this.page = page;
	}
	
	public Scope( T lower, T upper, int page){
		this( 0, lower, upper, page);
	}
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public T getLower() {
		return lower;
	}
	public void setLower(T lower) {
		this.lower = lower;
	}
	public T getUpper() {
		return upper;
	}
	public void setUpper(T upper) {
		this.upper = upper;
	}

	public void setPage(int page) {
		this.page = page;
	}

	public int getPage() {
		return page;
	}
}
