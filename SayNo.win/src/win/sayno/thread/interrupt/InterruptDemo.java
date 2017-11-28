package win.sayno.thread.interrupt;

import java.util.ArrayList;
import java.util.List;

/**
 * 论证使用interrupt中断线程,线程状态为join,sleep,wait时会抛出InterruptedException
 * @author SayNo
 *
 */
public class InterruptDemo {
	
	public static void main(String[] args) {
		// SleepThread 测试Thread调用Sleep方法时,中断线程，是否抛出InterruptedException
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
//		// JoinThread 测试Thread调用join方法时,中断线程，是否抛出InterruptedException
//		Thread joinThread = new Thread(new JoinThread(sleepThread));
//		joinThread.start();
//		joinThread.setName("joinThread");
//		try {
//			Thread.sleep(2000);
//			joinThread.interrupt();
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
		
		// WaitThread 测试Thread处于wait状态时,中断线程，是否抛出InterruptedException
		Lists lists = new Lists();
		Thread waitThread = new Thread(new WaitThread(lists));
		waitThread.setName("waitThread");
		waitThread.start();
		try {
			Thread.sleep(2000);
			waitThread.interrupt();
			// 主线程获取对象lists锁
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
					+ "执行结束");
		} catch (InterruptedException e) {
			System.out.println(Thread.currentThread().getName()
					+ "抛出了InterruptedException");
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
				// join方法原理就是判断当前执行代码的线程是否存活,如果存活this.wait
				// this就是sleepThread,如果sleepThread执行完毕则当前线程肯定获取到锁
				sleepThread.join();
			}
			Thread.sleep(50000);
		} catch (InterruptedException e) {
			System.out.println(Thread.currentThread().getName()
					+ "抛出了InterruptedException");
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
			System.out.println("添加元素:" + str);
//			this.notifyAll();
		}
	}
	
	public void remove(int index) {
		synchronized(lists) {
			if (lists.size() <= 0) {
				try {
					lists.wait();
				} catch (InterruptedException e) {
					System.out.println(Thread.currentThread().getName() + "等待中抛出了InterruptedException");
					return;
				}
			}
			lists.remove(index);
			System.out.println("删除第一个元素结束");

		}
	}
}