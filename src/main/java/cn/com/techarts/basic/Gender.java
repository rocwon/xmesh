package cn.com.techarts.basic;

public enum Gender {
	Unknown(0),
	Male(1),
	Female(2);
	
	private int id = 0;
	
	private Gender(int id) {
		this.setId(id);
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	
	public static Gender to(int gender) {
		return Gender.values()[gender];
	}
}
