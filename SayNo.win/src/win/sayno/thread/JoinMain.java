package win.sayno.thread;

public class JoinMain implements Runnable{

	public static int i = 0;
	
	@Override
	public void run() {
		for (i=0;i< 20;i++);
	}
	
	public static void main(String[] args) {
		Thread thread = new Thread(new JoinMain());
		thread.start();
		try {
			// 设置主线程等待子线程执行完毕后执行
			thread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		try {
			Thread.sleep(10000);
			System.out.println("12121" + Thread.currentThread());
			System.out.println("main i : " + i);
			System.out.println(thread == null);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
	}
}
