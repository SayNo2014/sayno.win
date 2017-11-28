package win.sayno.thread;

import java.util.ArrayList;
import java.util.List;

public class ThreadDeadLock {
	
	public static void main(String[] args) {
		Thread t1 = new Thread(new MyThread1());
		t1.start();
		Thread t2 = new Thread(new MyThread1());
		t2.start();
	}
}

class MyThread1 implements Runnable {

	private List<Object> lists = new ArrayList<Object>();
	
	@Override
	public void run() {
		method1();
	}
	
	public synchronized void method1() {
		try {
			for (int i=0;i<(1>>20);i++) {
				lists.add(new Object());
			}
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		method2();
	}
	
	public synchronized void method2() {
		try {
			for (int i=0;i<(1>>20);i++) {
				lists.add(new Object());
			}
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		method1();
	}
}