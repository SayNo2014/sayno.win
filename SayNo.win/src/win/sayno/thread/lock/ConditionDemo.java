package win.sayno.thread.lock;

import java.util.concurrent.locks.ReentrantLock;

public class ConditionDemo {
	public static void main(String[] args) {
		ReentrantLock lock = new ReentrantLock();
		Thread thread1 = new Thread(new MyThread(lock));
		Thread thread2 = new Thread(new MyThread(lock));
		thread1.start();
		try {
			Thread.sleep(500);
			thread2.start();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
