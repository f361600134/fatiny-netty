package com.jeremy.nio;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class TimeClientHandler implements Runnable{
	
	private String host = "127.0.0.1";
	private int port;
	
	private Selector selector;
	private SocketChannel socketChannel;
	private volatile boolean stop;
	
	/**
	 * 构造函数初始化NIO的多路复用和SocketChannel
	 * 初始化成功后需要将其设置为异步模式
	 * @param host
	 * @param port
	 */
	public TimeClientHandler(String host, int port){
		this.host = host == null ? this.host : host;
		this.port = port;
		try {
			selector = selector.open();
			socketChannel = SocketChannel.open();
			socketChannel.configureBlocking(false);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	@Override
	public void run() {
		try {
			/*
			 * 发送链接请求, 这里作为示例, 连接是成功的.
			 */
			doConnect();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		/*
		 * 轮询多路复用selector, 当有可用的Channel时, 执行handlerInput();
		 */
		while(!stop){
			try {
				selector.select();
				Set<SelectionKey> selectionKeys = selector.selectedKeys();
				Iterator<SelectionKey> iter = selectionKeys.iterator();
				SelectionKey key = null;
				while (iter.hasNext()){
					key = (SelectionKey) iter.next();
					iter.remove();
					try {
						handlerInput(key);
					} catch (Exception e) {
						if (key != null){
							key.cancel();
							if (key.channel() != null) 
								key.channel().close();
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		/*
		 * 对资源进行优雅的释放.
		 */
		if (selector != null) {
			try {
				selector.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
	}
	
	/**
	 * 如果key是有效的, 则对key连接状态进行判断, 如果处于连接状态, 则说明服务器已经返回ACK应答消息
	 * 这时只需要对连接结果进行判断, 调用SocketChannel的finishConnect方法, 如果返回值为true, 
	 * 表明连接成功, 把读事件, 写在多路复用器上, 然后调用write把内容写回给服务器.
	 * 
	 * @param key
	 */
	private void handlerInput(SelectionKey key) throws Exception{
		if (key.isValid()) {
			SocketChannel sc = (SocketChannel)key.channel();
			if (key.isConnectable()) {
				if (sc.finishConnect()) {
					sc.register(selector, SelectionKey.OP_READ);
					doWrite(sc);
				}else {
					System.exit(1);//连接失败, 退出连接
				}
			}
			
			/*
			 * 如果客户端接收到了服务器的应答消息, 则SocketChannel是可读的, 
			 * 由于无法实现判断流的大小, 所以预先分配1m接受缓存区用于应答
			 * 消息, 调用SocketChannle.read()用于异步读取操作. 这部分跟服务器相似, 不在赘述
			 * 读到消息后, 对消息进行解码, 最后设置stop为ture
			 */
			if (key.isReadable()) {
				ByteBuffer readBuffer = ByteBuffer.allocate(1024);
				int readByts = sc.read(readBuffer);
				if (readByts > 0) {
					readBuffer.flip();
					byte[] bytes = new byte[readBuffer.remaining()];
					readBuffer.get(bytes);
					String body = new String(bytes, "UTF-8");
					System.out.println("Now is:" + body);
					this.stop = true;
				}else if(readByts < 0){
					key.cancel();
					sc.close();
				}else{
					//do nothing.
					System.out.println("~");
				}
			}
		}
	}
	
	/**
	 *  处理连接
	 *  如果连接成功, 注册读read事件到selector, 否则注册连接事件connect
	 *  如果没有返回TCP 握手应答信息, 并不代表连接失败.
	 */
	private void doConnect() throws Exception{
		if (socketChannel.connect(new InetSocketAddress(host, port))) {
			socketChannel.register(selector, SelectionKey.OP_READ);
			doWrite(socketChannel);
		}else{
			socketChannel.register(selector, SelectionKey.OP_CONNECT);
		}
	}	
	
	/**
	 * 构造处理写操作
	 * @param socketChannel
	 */
	private void doWrite(SocketChannel socketChannel) throws Exception{
		byte[] req = "Query Time Order".getBytes();
		ByteBuffer writeBuffer = ByteBuffer.allocate(req.length);
		writeBuffer.put(req);
		writeBuffer.flip();
		socketChannel.write(writeBuffer);
		if (!writeBuffer.hasRemaining()) {
			System.out.println("Send order 2 server succeed");
		}
	}
	
	
}
