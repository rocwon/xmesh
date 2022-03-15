package cn.com.techarts.ioc;

import javax.inject.*;

import cn.com.techarts.basic.UniqueObject;

@Singleton
public class DiTests {
	
	@Inject
	public DiTests(@Named("user") UniqueObject a, int i) {
		
	}
	
	public static void main(String[] args) {
		var clazz = DiTests.class;
		var cs = clazz.getConstructors();
		for(var c : cs) {
			var ps = c.getParameters();
			var pas = c.getParameterAnnotations();
			var p1 = pas[0][0];
			System.out.println(clazz.getSimpleName());
		}
	}
	
	public int getAge() {
		return age;
	}

	@Named("aaaa")
	public void setAge(int age) {
		this.age = age;
	}

	@Inject
	private int age;
}
