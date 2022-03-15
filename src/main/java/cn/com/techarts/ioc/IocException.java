package cn.com.techarts.ioc;

public class IocException extends RuntimeException {
	public IocException(String cause) {
		super(cause);
	}
	
	public IocException(String cause, Throwable throwable) {
		super(cause, throwable);
	}
	
	public static IocException nullName() {
		return new IocException("The bean name is null");
	}
	
	public static IocException notFound(String name) {
		return new IocException("Can't find the bean with name [" + name + "]");
	}
	
	public static IocException cannotInstance(String name) {
		return new IocException("Can't call constructor to instance the bean [" + name + "]");
	}
	
	public static IocException noQualifier(String name) {
		return new IocException("You must qualify the constructor parameter[" + name + "]");
	}
}
