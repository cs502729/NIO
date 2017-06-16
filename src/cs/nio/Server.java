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

	//ͨ��������
	private Selector selector;
	
	/*
	 * ���һ��ServerSocket���󣬲����г�ʼ��
	 */
	public void initServer(int port) throws IOException{
		//���һ��serverSocketͨ��
		ServerSocketChannel serverChannel = ServerSocketChannel.open();
		//����ͨ��Ϊ������
		serverChannel.configureBlocking(false);
		//����ͨ����Ӧ��serverSocket�󶨵��̶��˿�
		serverChannel.socket().bind(new InetSocketAddress(port));
		//���һ��ͨ��������
		this.selector = Selector.open();
		//��ͨ���������͸�ͨ���󶨣���Ϊ����ͨ��ע��ACCEPT�¼���ע����¼��󣬵��¼�����ʱ��selector.selct()�᷵�أ�������¼�û�����һֱ����
		serverChannel.register(selector, SelectionKey.OP_ACCEPT);
	}
	
	/**
	 * ������ѯ�ķ�������ͨ�����Ƕ��пɱ�������¼����У�����д���
	 * @throws IOException 
	 */
	public void listen() throws IOException{
		System.out.println("���������");
		while(true){
		//��ע����¼�����ʱ���������أ�����,�÷�����һֱ���� 
			selector.select();
			// ���selector��ѡ�е���ĵ�������ѡ�е���Ϊע����¼�  
			Iterator it  = this.selector.selectedKeys().iterator();
			while(it.hasNext()){
				SelectionKey key = (SelectionKey)it.next();
				//ɾ���ض������ֹ�ظ�����
				it.remove();
				//�ͻ������������¼�
				if(key.isAcceptable()){
					ServerSocketChannel server = (ServerSocketChannel) key.channel();
					//��úͿͻ������ӵ�ͨ��
					SocketChannel channel = server.accept();
					//���óɷ�����
					channel.configureBlocking(false);
					//���ͻ��˷�����Ϣ
					channel.write(ByteBuffer.wrap(new String("server reply").getBytes()));
				    //�ںͿͻ������ӳɹ�֮��Ϊ�˿��Խ��յ��ͻ��˵���Ϣ����Ҫ��ͨ�����ö���Ȩ);
					channel.register(selector, SelectionKey.OP_READ);
				}
				else if(key.isReadable()){
					//��ȡ�ɶ��¼�
					read(key);
				}
			}
		}
	}
	
	public void read(SelectionKey key) throws IOException{
		// �������ɶ�ȡ��Ϣ:�õ��¼�������Socketͨ��  
		SocketChannel channel = (SocketChannel) key.channel();
		//������ȡ�Ļ�����
		ByteBuffer buffer = ByteBuffer.allocate(20);
		channel.read(buffer);
		byte[] data =  buffer.array();
		String str = new String(data).trim();
		System.out.println("������յ��� "+str);
		ByteBuffer outBuffer = ByteBuffer.wrap(str.getBytes());
		//���͸��ͻ���
		channel.write(outBuffer);
	}
	
	public static void main(String[] args) throws IOException {
		Server server = new Server();
		server.initServer(9999);
		server.listen();		
	}
}
