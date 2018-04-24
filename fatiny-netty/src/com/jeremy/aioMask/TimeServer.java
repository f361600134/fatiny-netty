package com.jeremy.aioMask;

import java.net.ServerSocket;
import java.net.Socket;

/**
 * <B>伪装AIO</B></p>
 * 
 * 伪异步IO, 对于服务器监听做了优化, 对socket进行了封装.</p>
 * 
 * 利用线程池, 避免了每一个请求都创建相应的链接.相比较于传统的IO,是一种改良.</p>
 * 
 * 但是他底层依旧是用了同步阻塞模型, 因此无法从根本上解决问题.</p>
 * 
 * 当调用OutputStream中的write写输出流的时候,他将会被阻塞, 直到所有的输出字节写入完毕, 或者发生异常.
 * 
 * 读和写都是同步阻塞的, 当所有的可用线程都因为故障被阻塞, 后续所有的IO都将在队列中排队.当队列挤满之后, 后续如队列操作将会被阻塞.
 * 
 * 此时,客户端将会产生大量的连接超时.
 * 
 * @author Administrator
 */
public class TimeServer {
	
	public static void main(String[] args) {
		int port = 8080;
		try {
			ServerSocket server = new ServerSocket(port);
			System.out.println("The time server is start in port:"+port);
			
			TimeHandlerExcutorPool executorPool = new TimeHandlerExcutorPool(50, 100);
			Socket socket = null;
			while (true) {
				socket = server.accept();
				//new Thread(new TimeServerHandlder(socket)).start();
				executorPool.execute(new TimeServerHandlder(socket));
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
