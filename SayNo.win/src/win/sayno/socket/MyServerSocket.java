package win.sayno.socket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class MyServerSocket {
	public static void main(String[] args) {
		try {
			// 1.����ServerSocket
			ServerSocket serverSocket = new ServerSocket(8080);
			// 2.�ȴ�����
			Socket socket = serverSocket.accept();
			// 3.���������ʹ��Socket����ͨ�ţ�����BufferedReader���ڶ�ȡ����
			BufferedReader buffer = new BufferedReader(
					new InputStreamReader(socket.getInputStream()));
			String line = buffer.readLine();
			System.out.println("message from client : " + line);
			// 4.����PrintWrite�����ڷ�������
			PrintWriter pw = new PrintWriter(socket.getOutputStream());
			pw.write("received data :" + line);
			pw.flush();
			// 5.�ر���Դ
			pw.close();
			buffer.close();
			socket.close();
			serverSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
