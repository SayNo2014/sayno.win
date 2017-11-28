package win.sayno.thread;

public class ThreadWaitDemo {
	public static void main(String[] args) {
		// 1.创建线程的共享对象
		User user = new User();
		user.setName("sayno");
		user.setPassword("*******");
		
		Thread thread1 = new Thread(new MyThread(user, "thread1"));
		Thread thread2 = new Thread(new MyThread2(user, "thread2"));
		// 2.启动thread1
		thread1.start();
		try {
			// 3.主线程睡500ms，为了确保thread1优先与thread2执行
			Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		// 4.启动thread2
		thread2.start();
		// 注：sleep和wait都能让线程等待若干时间，但是wait可以被唤醒而且可以释放对象锁。
		// sleep不会释放任何资源 
		
	}
}
