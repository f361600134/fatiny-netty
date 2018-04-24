package com.jeremy.bio;

import java.net.ServerSocket;
import java.net.Socket;


/**
 * 实现一个BIO
 * 
 * 传统的BIO, 由一个独立的acceptor负责监听客户端连接, 当有了新的连接, 则创建新的线程去处理.
 * 
 * 当处理完成后, 通过输出流返回应答给客户端, 随后销毁线程. 这种是典型的一请求,一应答式的通讯模型.
 * 
 * 当访问量上来的时候,创建线程开销将会很大.
 * 
 * @author Administrator
 */
public class TimeServer {
	
	public static void main(String[] args) {
		int port = 8080;
		try {
			ServerSocket server = new ServerSocket(port);
			
			System.out.println("The time server is start in port:"+port);
			
			Socket socket = null;
			while (true) {
				socket = server.accept();
				
				new Thread(new TimeServerHandlder(socket)).start();
				
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
