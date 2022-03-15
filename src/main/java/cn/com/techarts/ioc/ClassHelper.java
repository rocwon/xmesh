package cn.com.techarts.ioc;

import java.util.List;

public final class ClassHelper {
	public List<Object> scanPackage(String path){
		if(path == null) return null;
		return List.of();
	}
}
