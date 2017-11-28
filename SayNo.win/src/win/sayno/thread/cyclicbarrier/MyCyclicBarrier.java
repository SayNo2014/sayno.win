package win.sayno.thread.cyclicbarrier;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

public class MyCyclicBarrier {
	static CyclicBarrier cyclicBarrier = null;
	public static void main(String[] args) {
		cyclicBarrier = new CyclicBarrier(4, new Runnable(){
			@Override
			public void run() {
				System.out.println("所有学员都准备好了,开始答题...");
			}
		});
		MyThread myThread = null;
		Thread thread;
		for (int i=0;i < 4;i++) {
			myThread = new MyThread();
			myThread.setCyclicBarrier(cyclicBarrier);
			thread = new Thread(myThread);
			thread.setName("thread" + i);
			thread.start();
		}
	}
}

class MyThread implements Runnable {
	
	CyclicBarrier cyclicBarrier;
	
	@Override
	public void run() {
		try {
			System.out.println(Thread.currentThread().getName() + "学员准备好了...");
			cyclicBarrier.await();
			System.out.println(
					Thread.currentThread().getName() + "学员答题完毕...");
		} catch (InterruptedException | BrokenBarrierException e) {
			e.printStackTrace();
		}
		
	}

	public CyclicBarrier getCyclicBarrier() {
		return cyclicBarrier;
	}

	public void setCyclicBarrier(CyclicBarrier cyclicBarrier) {
		this.cyclicBarrier = cyclicBarrier;
	}
	
}