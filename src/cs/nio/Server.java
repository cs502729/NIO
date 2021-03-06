package cs.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

public class Server {

	//通道管理器
	private Selector selector;
	
	/*
	 * 获得一个ServerSocket对象，并进行初始化
	 */
	public void initServer(int port) throws IOException{
		//获得一个serverSocket通道
		ServerSocketChannel serverChannel = ServerSocketChannel.open();
		//设置通过为非阻塞
		serverChannel.configureBlocking(false);
		//将该通道对应的serverSocket绑定到固定端口
		serverChannel.socket().bind(new InetSocketAddress(port));
		//获得一个通道管理器
		this.selector = Selector.open();
		//将通道管理器和该通道绑定，并为该热通道注册ACCEPT事件，注册该事件后，当事件到达时，selector.selct()会返回，如果该事件没到达会一直阻塞
		serverChannel.register(selector, SelectionKey.OP_ACCEPT);
	}
	
	/**
	 * 采用轮询的方法监听通道中是都有可被处理的事件，有，则进行处理
	 * @throws IOException 
	 */
	public void listen() throws IOException{
		System.out.println("服务端启动");
		while(true){
		//当注册的事件到达时，方法返回；否则,该方法会一直阻塞 
			selector.select();
			// 获得selector中选中的项的迭代器，选中的项为注册的事件  
			Iterator it  = this.selector.selectedKeys().iterator();
			while(it.hasNext()){
				SelectionKey key = (SelectionKey)it.next();
				//删除重读的项，防止重复处理
				it.remove();
				//客户端请求连接事件
				if(key.isAcceptable()){
					ServerSocketChannel server = (ServerSocketChannel) key.channel();
					//获得和客户端连接的通道
					SocketChannel channel = server.accept();
					//设置成非阻塞
					channel.configureBlocking(false);
					//给客户端发送信息
					channel.write(ByteBuffer.wrap(new String("server reply").getBytes()));
				    //在和客户端连接成功之后，为了可以接收到客户端的信息，需要给通道设置读的权);
					channel.register(selector, SelectionKey.OP_READ);
				}
				else if(key.isReadable()){
					//获取可读事件
					read(key);
				}
			}
		}
	}
	
	public void read(SelectionKey key) throws IOException{
		// 服务器可读取消息:得到事件发生的Socket通道  
		SocketChannel channel = (SocketChannel) key.channel();
		//创建读取的缓冲区
		ByteBuffer buffer = ByteBuffer.allocate(20);
		channel.read(buffer);
		byte[] data =  buffer.array();
		String str = new String(data).trim();
		System.out.println("服务端收到： "+str);
		ByteBuffer outBuffer = ByteBuffer.wrap(str.getBytes());
		//回送给客户端
		channel.write(outBuffer);
	}
	
	public static void main(String[] args) throws IOException {
		Server server = new Server();
		server.initServer(9999);
		server.listen();		
	}
}
