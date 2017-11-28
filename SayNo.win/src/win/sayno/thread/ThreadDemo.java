package win.sayno.thread;

public class ThreadDemo implements Runnable {
	
	public static void main(String[] args) {
		// 1.创建线程thread -- State.NEW
		Thread thread = new Thread(new ThreadDemo());
		// 2.开启线程 -- State.RUNNABLE
		thread.start();
		try {
			// 3.调用Thread.sleep()方法会让线程休眠2000ms
			Thread.sleep(2000);
			// 4.设置线程thread的中断标识
			thread.interrupt();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		// 轮询
		while(true) {
			// 该线程判断该线程的中断状态，若2s后thread线程被设置了中断标识，该轮询中断
			if(Thread.currentThread().isInterrupted()) {
				System.out.println("Thread Interrupted ...");
				break;
			}
			Thread.yield();
		}
	}
}
