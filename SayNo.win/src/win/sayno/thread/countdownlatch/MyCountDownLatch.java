package win.sayno.thread.countdownlatch;

import java.util.concurrent.CountDownLatch;

public class MyCountDownLatch {
	
	private static CountDownLatch countDownLatch = null;
	
	public static void main(String[] args) {
		countDownLatch = new CountDownLatch(6);
		Thread thread = null;
		for (int i=0;i<6;i++) {
			thread = new Thread(new MyThread());
			thread.setName("thread" + i);
			thread.start();
		}
		try {
			countDownLatch.await();
			System.out.println("所有用户都完成问券调查,可以进行统计分析。");
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	static class MyThread implements Runnable {

		@Override
		public void run() {
			System.out.println(
					Thread.currentThread().getName() + "完成问卷调查");
			countDownLatch.countDown();
		}
	}
}
