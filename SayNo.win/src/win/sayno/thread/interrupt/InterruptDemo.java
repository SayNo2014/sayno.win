package win.sayno.thread.interrupt;

import java.util.ArrayList;
import java.util.List;

/**
 * ��֤ʹ��interrupt�ж��߳�,�߳�״̬Ϊjoin,sleep,waitʱ���׳�InterruptedException
 * @author SayNo
 *
 */
public class InterruptDemo {
	
	public static void main(String[] args) {
		// SleepThread ����Thread����Sleep����ʱ,�ж��̣߳��Ƿ��׳�InterruptedException
//		Thread sleepThread = new Thread(new SleepThread());
//		sleepThread.setName("sleepThread");
//		sleepThread.start();
//		try {
//			Thread.sleep(2000);
////			sleepThread.interrupt();
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
//		
//		// JoinThread ����Thread����join����ʱ,�ж��̣߳��Ƿ��׳�InterruptedException
//		Thread joinThread = new Thread(new JoinThread(sleepThread));
//		joinThread.start();
//		joinThread.setName("joinThread");
//		try {
//			Thread.sleep(2000);
//			joinThread.interrupt();
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
		
		// WaitThread ����Thread����wait״̬ʱ,�ж��̣߳��Ƿ��׳�InterruptedException
		Lists lists = new Lists();
		Thread waitThread = new Thread(new WaitThread(lists));
		waitThread.setName("waitThread");
		waitThread.start();
		try {
			Thread.sleep(2000);
			waitThread.interrupt();
			// ���̻߳�ȡ����lists��
//			lists.add("sayno");
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		
	}
}
class SleepThread implements Runnable {

	@Override
	public void run() {
		try {
			Thread.sleep(5000);
			System.out.println(Thread.currentThread().getName()
					+ "ִ�н���");
		} catch (InterruptedException e) {
			System.out.println(Thread.currentThread().getName()
					+ "�׳���InterruptedException");
		}
	}
}

class JoinThread implements Runnable {
	Thread sleepThread = null;
	
	public JoinThread(Thread thread) {
		this.sleepThread = thread;
	}
	
	@Override
	public void run() {
		try {
			if (sleepThread != null) {
				// join����ԭ������жϵ�ǰִ�д�����߳��Ƿ���,������this.wait
				// this����sleepThread,���sleepThreadִ�������ǰ�߳̿϶���ȡ����
				sleepThread.join();
			}
			Thread.sleep(50000);
		} catch (InterruptedException e) {
			System.out.println(Thread.currentThread().getName()
					+ "�׳���InterruptedException");
		}
	}
}

class WaitThread implements Runnable {
	
	private Lists lists;
	
	public WaitThread(Lists lists) {
		this.lists = lists;
	}
	
	@Override
	public void run() {
		lists.remove(0);
	}

	public Lists getLists() {
		return lists;
	}

	public void setLists(Lists lists) {
		this.lists = lists;
	}
}

class Lists {
	
	private List<String> lists = new ArrayList<String>();
	
	public void add(String str) {
		synchronized(lists) {
			lists.add(str);
			System.out.println("���Ԫ��:" + str);
//			this.notifyAll();
		}
	}
	
	public void remove(int index) {
		synchronized(lists) {
			if (lists.size() <= 0) {
				try {
					lists.wait();
				} catch (InterruptedException e) {
					System.out.println(Thread.currentThread().getName() + "�ȴ����׳���InterruptedException");
					return;
				}
			}
			lists.remove(index);
			System.out.println("ɾ����һ��Ԫ�ؽ���");

		}
	}
}