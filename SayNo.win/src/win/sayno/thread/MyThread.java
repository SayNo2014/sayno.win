package win.sayno.thread;

public class MyThread implements Runnable{

	private User user;
	
	private String name;
	
	public MyThread(User user, String name) {
		this.user = user;
		this.name = name;
	}
	
	@Override
	public void run() {
		// thread1先申请了user的对象锁，当调用了wait方法，释放了user锁
		synchronized(user) {
			System.out.println(System.currentTimeMillis()
					+ ":" + name + " start !");
			try {
				System.out.println(System.currentTimeMillis()
						+ ":" + name + " wait user lock ...");
				user.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			// thread2执行时调用了notify，thread2执行完成后释放user锁，
			// thread1继续得到user锁，然后继续执行
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
