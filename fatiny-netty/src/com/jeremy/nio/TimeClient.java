package com.jeremy.nio;


/**
 * NIO 创建的客户端客户端改造
 * 
 * @author Administrator
 *
 */
public class TimeClient {
	
	public static void main(String[] args) {
		String ip = "127.0.0.1";
		int port = 8080;
		
		try {
			new Thread(new TimeClientHandler(ip, port)).start();
		} catch (Exception e) {
			e.printStackTrace();
		}
		}

}
