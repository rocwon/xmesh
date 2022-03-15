package cn.com.techarts.msx;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface WebServiceExporter {
	/**
	 * The property value is same to the object name in SPRING-IOC
	 */
	public String name();
}
