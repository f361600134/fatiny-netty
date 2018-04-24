package com.jeremy.nio;



/**
 * 实现一个NIO
 * 
 * 优点: 
 *	1.客户端发起的连接操作是异步的, 可以通过在多路复用器注册OP_CONNECT等待后续结果, 而不是被阻塞.
 *	2.SocketChannel的读写操作都是异步的, 如果没有可读写的数据他不会同步等待, 直接返回, 这样IO
 *		通讯线程就会处理其他链路, 而不是同步等待这个链路可用.
 *	3.线程模型的优化, 由于JDK的selector在Linux主流操作系统通过epoll实现, 他没有链接句柄数的限制(
 *		只受限于操作系统的最大链接句柄数或者对单条线程连接句柄数的限制) 这就意味着一个selector可以同时
 *		处理成千上万个客户端连接,而且不会应为客户端的连接而线性下降. 因为它非常适合做高性能,高负载的网络服务器.
 * 
 * 
 * @author Administrator
 */
public class TimeServer {
	
	public static void main(String[] args) {
		int port = 8080;
		try {
			//ServerSocket server = new ServerSocket(port);
			
			//System.out.println("The time server is start in port:"+port);
			
//			Socket socket = null;
//			while (true) {
//				socket = server.accept();
//				
//				new Thread(new TimeServerHandlder(socket)).start();
//				
//			}
			
			MultiplexerTimeServer timeServer = new MultiplexerTimeServer(port);
			new Thread(timeServer, "NIO-001").start();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
