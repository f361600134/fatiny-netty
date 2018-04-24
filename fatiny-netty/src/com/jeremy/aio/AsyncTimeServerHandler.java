package com.jeremy.aio;

import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.util.concurrent.CountDownLatch;

public class AsyncTimeServerHandler implements Runnable {
	
	private int port;
	CountDownLatch latch;
	AsynchronousServerSocketChannel channel;
	
	/**
	 * 创建服务器异步通道, 绑定地址
	 * @param port
	 */
	public AsyncTimeServerHandler(int port){
		this.port = port;
		try {
			channel.open();
			channel.bind(new InetSocketAddress(port));
			System.out.println("The time server is start in port:"+port);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		latch = new CountDownLatch(1);
		doAccept();
		try {
			latch.await();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void doAccept(){
		channel.accept(this, new AcceptCompletionHandler());
	}
	
	
}































