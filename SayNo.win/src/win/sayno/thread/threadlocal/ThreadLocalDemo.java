package win.sayno.thread.threadlocal;

import java.util.ArrayList;
import java.util.List;

public class ThreadLocalDemo {
	public static void main(String args[]) {
		MyThread thread = new MyThread();
		
		List<Thread> threads = new ArrayList<Thread>();
		
		for (int i=1;i<10;i++) {
			threads.add(new Thread(thread));
		}
		
		for (Thread trd : threads) {
			trd.start();
		}
	}
}
