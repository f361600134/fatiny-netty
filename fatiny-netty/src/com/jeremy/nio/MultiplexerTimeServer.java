package com.jeremy.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;

/**
 * NIO 时间服务器
 */
public class MultiplexerTimeServer implements Runnable{
	
	private Selector selector;
	
	private ServerSocketChannel serverChannel;
	
	private volatile boolean stop;
	
	/**
	 * 初始化多路复用器, 绑定监听端口
	 * 构造方法初始化资源, 创建多路复用selector, serverSocketChannel, 对Channel和TCP参数进行配置, 例如, 
	 * 将ServerSocketChannal设置为异步非阻塞模式, 它的backlog 为1024, 系统资源初始化成功后, 将ServerSocketChannel
	 * 注册岛selector, 监听SelectionKey.OP_ACCEPT操作位, 如果操作失败(比如端口被占用), 则退出.
	 * @param port
	 */
	@SuppressWarnings("static-access")
	public MultiplexerTimeServer(int port){
		try {
			selector = selector.open();
			serverChannel = ServerSocketChannel.open();
			serverChannel.configureBlocking(false);
			serverChannel.socket().bind(new InetSocketAddress(port), 1024);
			serverChannel.register(selector, SelectionKey.OP_ACCEPT);
			System.out.println("The time server is start in port:"+port);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void stop(){
		this.stop = true;
	}
	
	/**
	 * while循环遍历seletor, 休眠时间为1秒, 无论是否有读写事件发生, selector 都会被唤醒一次,
	 * selector也提供了一个无参的select. 当有处于就绪的Channel的时候,selector就返回就绪的状态的
	 * Chennel的selectionKey集合, 通过对就绪状态的Channel集合进行迭代, 可以进行网络的异步读写操作
	 */
	@Override
	public void run() {
		while (!stop) {
			try {
				selector.select(1000);
				
				Set<SelectionKey> selectedKeys = selector.selectedKeys();
				Iterator<SelectionKey> it = selectedKeys.iterator();
				SelectionKey key = null;
				while(it.hasNext()){
					key = it.next();
					it.remove();
					try {
						handleInput(key);
					} catch (Exception e) {
						if(key != null){
							key.cancel();
							if (key.channel() != null) {
								key.channel().close();
							}
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (selector != null) {
			try {
				selector.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * 处理客户端消息, 根据selectionKey的操作位进行判断读写事件类型, 通过ServerSocketChannel的accept接收客户端链接请求并
	 * 创建SocketChannel实例, 完成上述操作后, 相当于完成了TCP的三次握手, TCP物理链路正式连接. 注意, 我们需要将新创建的SocketChannel
	 * 设置为异步非阻塞, 同时也可以对TCP进行参数设置, 例如TCP接受和发送的缓存区大小.
	 * @param key
	 * @throws Exception
	 */
	private void handleInput(SelectionKey key) throws Exception{
		if (key.isValid()) {
			if (key.isAcceptable()) {
				//Access the new connection.
				ServerSocketChannel ssc = (ServerSocketChannel)key.channel();
				SocketChannel sc = ssc.accept();
				sc.configureBlocking(false);
				//Add the new connection to the selector.
				sc.register(selector, SelectionKey.OP_READ);
			}
			/*
			 * 这里是用于读取客户端请求, 首先创建byteBuffer, 由于我们无法事先得知客户端发送的码流大小, 作为例子, 我们创建
			 * 一个1K的缓冲区, 然后调用SocketChannel的read方法读取请求的码流.由于我们将SocketChannel设置为异步非阻塞,
			 * 所以read是非阻塞的. 使用返回值进行判断, 看到读取字节数, 返回值有一下三种可能
			 *	1.返回值大于0, 读到了字节, 对字节进行解码
			 *	2.返回值等于0, 没有读到字节,属于正常场景, 忽略.
			 *	3.返回值为-1, 链路已经关闭, 需要关闭SocketChannel, 释放资源.
			 * 当读到码流之后, 我们进行解码, 首先对readbuffer进行flip操作, 它的作用是将缓冲区当前的limit设置为position,
			 * position设置为0, 用于后续对缓冲区的读取操作. 然后根据缓冲区可读字节个数创建数组, 调用ByteBuffer中的get将缓冲区的数组
			 * 复制到新创建的数组中, 最后调用字符串调用创建请求消息并打印, 如果请求指令是"Query time server", 就把当前服务器编码后的字符串
			 * 返回给客户端.
			 */
			if (key.isReadable()) {
				//Read the data 
				SocketChannel sc = (SocketChannel)key.channel();
				ByteBuffer readbuffer = ByteBuffer.allocate(1024);
				int readBytes = sc.read(readbuffer);
				if (readBytes > 0) {
					readbuffer.flip();
					byte[] bytes = new byte[readbuffer.remaining()];
					readbuffer.get(bytes);
					String body = new String(bytes, "UTF-8");
					System.out.println("The time server recive order:"+body);
					String currentTime = "Query time server".equalsIgnoreCase(body) ?
							new Date(System.currentTimeMillis()).toString():"Bad order";
					
					doWtite(sc, currentTime);
				}
			}
		}
	}

	/**
	 * 将消息异步发送给客户端, 根据数组容量创建一个bytes, 调用put方法将字节数组复制到缓冲区内. 对缓冲区进行flip操作, 最后调用
	 * SocketChannel 将字节数组发送回去
	 * @param sc
	 * @param response
	 * @throws IOException
	 */
	private void doWtite(SocketChannel sc, String response) throws IOException{
		if (response != null && response.trim().length() > 0) {
			byte[] bytes = response.getBytes();
			ByteBuffer writebuffer = ByteBuffer.allocate(bytes.length);
			writebuffer.put(bytes);
			writebuffer.flip();
			sc.write(writebuffer);
		}
	}
	
}
