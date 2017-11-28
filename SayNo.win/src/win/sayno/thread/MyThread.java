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
		// thread1��������user�Ķ���������������wait�������ͷ���user��
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
			// thread2ִ��ʱ������notify��thread2ִ����ɺ��ͷ�user����
			// thread1�����õ�user����Ȼ�����ִ��
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
