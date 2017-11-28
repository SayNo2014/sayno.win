package win.sayno.thread.lock;

import java.util.concurrent.locks.ReentrantLock;

public class ReentrantLockDemo implements Runnable{

	private static int i = 0;
	
	private static ReentrantLock lock = new ReentrantLock(); 
	
	@Override
	public void run() {
		
		for (int j=0; j< 10000; j++) {
			lock.lock();
			try {
				i++;
			} finally {
				lock.unlock();
			}
			
			lock.lock();
			i= i +2 ;
			lock.unlock();
		}
		System.out.println(this);
	}
	
	public static void main(String[] args) {
		Thread thread1 = new Thread(new ReentrantLockDemo());
		Thread thread2 = new Thread(new ReentrantLockDemo());
		Thread thread3 = new Thread(new ReentrantLockDemo());
		thread1.start();
		thread2.start();
		thread3.start();
		try {
			thread1.join();
			thread2.join();
			thread3.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println(i);
	}

}
