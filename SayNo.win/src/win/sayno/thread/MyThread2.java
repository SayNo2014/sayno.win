package win.sayno.thread;

public class MyThread2 implements Runnable{

	private User user;
	
	private String name;
	
	public MyThread2(User user, String name) {
		this.user = user;
		this.name = name;
	}
	
	@Override
	public void run() {
		// thread1调用wait方法，释放了user锁，thread2得到user锁，执行
		synchronized(user) {
			System.out.println(System.currentTimeMillis()
					+ ":" + name + " start !");
			// 声明释放user锁，但是曾到等到thread2执行完成后，才能释放user锁
			user.notify();
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			System.out.println(System.currentTimeMillis()
					+ ":" + name + " end !");
		}
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}
	
}
