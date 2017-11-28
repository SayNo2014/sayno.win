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
			// 1.创建ServerSocket
			ServerSocket serverSocket = new ServerSocket(8080);
			// 2.等待请求
			Socket socket = serverSocket.accept();
			// 3.接收请求后使用Socket进行通信，创建BufferedReader用于读取数据
			BufferedReader buffer = new BufferedReader(
					new InputStreamReader(socket.getInputStream()));
			String line = buffer.readLine();
			System.out.println("message from client : " + line);
			// 4.创建PrintWrite，用于发送数据
			PrintWriter pw = new PrintWriter(socket.getOutputStream());
			pw.write("received data :" + line);
			pw.flush();
			// 5.关闭资源
			pw.close();
			buffer.close();
			socket.close();
			serverSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
