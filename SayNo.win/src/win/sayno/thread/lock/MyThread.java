package win.sayno.thread.lock;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class MyThread implements Runnable {

	private ReentrantLock lock;
	
	private Condition condition;
	
	public MyThread(ReentrantLock lock) {
		this.lock = lock;
		this.condition = lock.newCondition();
	}
	@Override
	public void run() {
		System.out.println("thread1 start ...");
		lock.lock();
		System.out.println("thread1 await ...");
		try {
			condition.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("thread1 go on ...");
		lock.unlock();
		System.out.println("thread1 end ...");
	}
	
}
