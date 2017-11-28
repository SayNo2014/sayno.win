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
		// thread1����wait�������ͷ���user����thread2�õ�user����ִ��
		synchronized(user) {
			System.out.println(System.currentTimeMillis()
					+ ":" + name + " start !");
			// �����ͷ�user�������������ȵ�thread2ִ����ɺ󣬲����ͷ�user��
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
