package win.sayno.thread;

public class ThreadDemo implements Runnable {
	
	public static void main(String[] args) {
		// 1.�����߳�thread -- State.NEW
		Thread thread = new Thread(new ThreadDemo());
		// 2.�����߳� -- State.RUNNABLE
		thread.start();
		try {
			// 3.����Thread.sleep()���������߳�����2000ms
			Thread.sleep(2000);
			// 4.�����߳�thread���жϱ�ʶ
			thread.interrupt();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		// ��ѯ
		while(true) {
			// ���߳��жϸ��̵߳��ж�״̬����2s��thread�̱߳��������жϱ�ʶ������ѯ�ж�
			if(Thread.currentThread().isInterrupted()) {
				System.out.println("Thread Interrupted ...");
				break;
			}
			Thread.yield();
		}
	}
}
