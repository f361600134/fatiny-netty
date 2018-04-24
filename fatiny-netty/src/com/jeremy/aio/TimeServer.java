package com.jeremy.aio;



/**
 * 实现一个AIO
 * 
 * 
 * 
 * @author Administrator
 */
public class TimeServer {
	
	public static void main(String[] args) {
		int port = 8080;
		try {
			AsyncTimeServerHandler timeServer = new AsyncTimeServerHandler(port);
			new Thread(timeServer, "NIO-001").start();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
