package win.sayno.socket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class MySocketClient {
	public static void main(String[] args) {
		
		Socket socket = null;
		PrintWriter printWriter = null;
		BufferedReader read = null;
		
		try {
			socket = new Socket("127.0.0.1",8080);
			printWriter = new PrintWriter(socket.getOutputStream());
			printWriter.println("hello server,i'm sayno");
//			printWriter.write("hello server,i'm sayno");
			// 发送数据
			printWriter.flush();
			
			// 接收数据
			read = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			String message = read.readLine();
			System.out.println(message);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (printWriter != null) {
				printWriter.close();
			}
			
			if (read != null) {
				try {
					read.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
			if (socket != null) {
				try {
					socket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
