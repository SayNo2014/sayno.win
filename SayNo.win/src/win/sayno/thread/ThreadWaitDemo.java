package win.sayno.thread;

public class ThreadWaitDemo {
	public static void main(String[] args) {
		// 1.�����̵߳Ĺ������
		User user = new User();
		user.setName("sayno");
		user.setPassword("*******");
		
		Thread thread1 = new Thread(new MyThread(user, "thread1"));
		Thread thread2 = new Thread(new MyThread2(user, "thread2"));
		// 2.����thread1
		thread1.start();
		try {
			// 3.���߳�˯500ms��Ϊ��ȷ��thread1������thread2ִ��
			Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		// 4.����thread2
		thread2.start();
		// ע��sleep��wait�������̵߳ȴ�����ʱ�䣬����wait���Ա����Ѷ��ҿ����ͷŶ�������
		// sleep�����ͷ��κ���Դ 
		
	}
}
