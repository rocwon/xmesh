package cn.com.techarts.util;

import java.io.Serializable;

import com.alibaba.fastjson.JSON;

import cn.techarts.jhelper.Empty;

public class QueuedMessage implements Serializable {
	private int id;
	private String channel;
	private String message;
	
	public QueuedMessage(String channel, Object msg) {
		this.channel = channel;
		if(msg != null) {
			message = JSON.toJSONString(msg);
		}
	}
	
	public QueuedMessage(String channel, String msg) {
		this.channel = channel;
		this.message = msg;
	}
	
	public<T> T trans(Class<T> t) {
		if(t == null) return null;
		if(Empty.is(message)) return null;
		return JSON.parseObject(this.message, t);
	}
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getChannel() {
		return channel;
	}
	public void setChannel(String channel) {
		this.channel = channel;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}

}
