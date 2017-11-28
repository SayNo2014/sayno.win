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
			System.out.println("�����û��������ȯ����,���Խ���ͳ�Ʒ�����");
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	static class MyThread implements Runnable {

		@Override
		public void run() {
			System.out.println(
					Thread.currentThread().getName() + "����ʾ����");
			countDownLatch.countDown();
		}
	}
}
