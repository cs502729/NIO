package cs.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

public class Client {

	//ͨ��������
	private Selector selector;
	
	public void initClient(String ip,int port) throws IOException{
		// ���һ��Socketͨ��  
		SocketChannel channel = SocketChannel.open();
		channel.configureBlocking(false);
		this.selector = Selector.open();
		// �ͻ������ӷ�����,��ʵ����ִ�в�û��ʵ�����ӣ���Ҫ��listen���������е�  
        //��channel.finishConnect();�����������  
		channel.connect(new InetSocketAddress(ip, port));
		//��ͨ���������͸�ͨ���󶨣���Ϊ��ͨ��ע��SelectionKey.OP_CONNECT�¼�
		channel.register(selector, SelectionKey.OP_CONNECT);
	}
	
	 /** 
     * ������ѯ�ķ�ʽ����selector���Ƿ�����Ҫ������¼�������У�����д��� 
     * @throws IOException 
     */  
	public void listen() throws IOException{
		while(true){
			//��ע����¼�����ʱ���������أ�����,�÷�����һֱ���� 
			selector.select();
			Iterator it = this.selector.selectedKeys().iterator();
			while(it.hasNext()){
				SelectionKey key = (SelectionKey) it.next();
				//ɾ���ض������ֹ�ظ�����
				it.remove();
				// �����¼�����  
				if(key.isConnectable()){
					SocketChannel channel = (SocketChannel) key.channel();
					// ����������ӣ���������� 
					if(channel.isConnectionPending()){
						channel.finishConnect();
					}
					channel.configureBlocking(false);
					 //��������Ը�����˷�����Ϣ
					channel.write(ByteBuffer.wrap(new String("�ͻ��˷�����Ϣ").getBytes()));
					//�ںͷ�������ӳɹ�֮��Ϊ�˿��Խ��յ�����˵���Ϣ����Ҫ��ͨ�����ö���Ȩ
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
		System.out.println("�ͻ����յ��� "+str);
		ByteBuffer outBuffer = ByteBuffer.wrap(str.getBytes());
		
		channel.write(outBuffer);
	}
	public static void main(String[] args) throws IOException {
		Client client = new Client();
		client.initClient("localhost",9999);
		client.listen();
	}
}
