package cs.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

public class Client {

	//通道管理器
	private Selector selector;
	
	public void initClient(String ip,int port) throws IOException{
		// 获得一个Socket通道  
		SocketChannel channel = SocketChannel.open();
		channel.configureBlocking(false);
		this.selector = Selector.open();
		// 客户端连接服务器,其实方法执行并没有实现连接，需要在listen（）方法中调  
        //用channel.finishConnect();才能完成连接  
		channel.connect(new InetSocketAddress(ip, port));
		//将通道管理器和该通道绑定，并为该通道注册SelectionKey.OP_CONNECT事件
		channel.register(selector, SelectionKey.OP_CONNECT);
	}
	
	 /** 
     * 采用轮询的方式监听selector上是否有需要处理的事件，如果有，则进行处理 
     * @throws IOException 
     */  
	public void listen() throws IOException{
		while(true){
			//当注册的事件到达时，方法返回；否则,该方法会一直阻塞 
			selector.select();
			Iterator it = this.selector.selectedKeys().iterator();
			while(it.hasNext()){
				SelectionKey key = (SelectionKey) it.next();
				//删除重读的项，防止重复处理
				it.remove();
				// 连接事件发生  
				if(key.isConnectable()){
					SocketChannel channel = (SocketChannel) key.channel();
					// 如果正在连接，则完成连接 
					if(channel.isConnectionPending()){
						channel.finishConnect();
					}
					channel.configureBlocking(false);
					 //在这里可以给服务端发送信息
					channel.write(ByteBuffer.wrap(new String("客户端发送消息").getBytes()));
					//在和服务端连接成功之后，为了可以接收到服务端的信息，需要给通道设置读的权
					channel.register(selector, SelectionKey.OP_READ);
				}
				else if(key.isReadable()){
					read(key);
				}
				 
			}
		}
	}
	
	
	public void read(SelectionKey key) throws IOException{
		SocketChannel channel = (SocketChannel) key.channel();
		ByteBuffer buffer = ByteBuffer.allocate(20);
		channel.read(buffer);
		byte[] data =  buffer.array();
		String str = new String(data).trim();
		System.out.println("客户端收到： "+str);
		ByteBuffer outBuffer = ByteBuffer.wrap(str.getBytes());
		
		channel.write(outBuffer);
	}
	public static void main(String[] args) throws IOException {
		Client client = new Client();
		client.initClient("localhost",9999);
		client.listen();
	}
}
