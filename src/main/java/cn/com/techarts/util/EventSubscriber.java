package cn.com.techarts.util;

import redis.clients.jedis.JedisPubSub;

public class EventSubscriber extends JedisPubSub {
	
	/**
	 * Commonly, you should re-write the method to process your business.
	 */
	@Override
	public void onMessage(String channel, String message) {
		 System.out.println("I received message: " + message);
	}
}
