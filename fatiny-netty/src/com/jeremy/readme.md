一般来说, 低负载并发的程序可以选择同步阻塞IO来降低编程的复杂度.
但是对于高负载, 高并发的网络应用, 需要使用NIO的非阻塞模式进行开发.


常用ByteBuffer, 是因为便于网络读写

通道Channel
流是单向的, InputStream, OutputStram要么只用于读, 要么只用于写. Channel可以用于读和写

Channel分两类  适用于网络的SelectableChannel和适用于文件操作的FileChannel

多路复用Selector
Selector是多路复用的基础, 多路复用器提供选择已经就绪的人物的能力. 简单来说, Selector会不断轮询注册在其上的Channel, 如果某个Channel上面有新的TCP链接接入读写事件,
这个Channel就会处于就绪状态, 会被selector轮询出来,然后通过SelectionKey获取就绪的Channel集合,进行后续的IO操作.

一个多路复用器可以同时轮询多个Channel, 由于JDK使用了epoll来代替了传统的select实现, 所以它大连接句柄1024/2048的限制,这也就意味着只需要一个线程负责Selector的轮询,就可以接入成千上万个客户端,这是一个非常巨大的进步.

