package win.sayno.thread;

public class Test {
	public static void main(String[] args) {
		Godown godown = new Godown();
		
		Thread t1 = new Thread(new Consumer(godown, 30));
		t1.setName("t1");
		Thread t2 = new Thread(new Consumer(godown, 50));
		t2.setName("t2");
		Thread t3 = new Thread(new Consumer(godown, 20));
		t3.setName("t3");
		Thread t4 = new Thread(new Consumer(godown, 20));
		t4.setName("t4");
		Thread t5 = new Thread(new Consumer(godown, 40));
		t5.setName("t5");
		Thread t6 = new Thread(new Producer(godown, 20));
		t6.setName("t6");
		Thread t7 = new Thread(new Producer(godown, 10));
		t7.setName("t7");
		Thread t8 = new Thread(new Producer(godown, 10));
		t8.setName("t8");
		Thread t9 = new Thread(new Producer(godown, 20));
		t9.setName("t9");
		Thread t10 = new Thread(new Producer(godown, 80));
		t10.setName("t10");
		Thread t11 = new Thread(new Producer(godown, 20));
		t11.setName("t11");
		t1.start();
		t2.start();
		t3.start();
		t4.start();
		t5.start();
		t6.start();
		t7.start();
		t8.start();
		t9.start();
		t10.start();
		t11.start();
		
	}
}

class Godown {
	
	public static final int max_size = 100;
	
	public int curnum;
	
	public synchronized void produce(int neednum) {
		while (neednum + curnum > max_size) {
			try {
				System.out.println("----------------------------------------------------");
				System.out.println(Thread.currentThread().getName() + "需要生产：" + neednum);
				System.out.println("需要生产的产品数量：" + neednum + "超过库存剩余量" + (max_size - curnum) + "无法生产,请等待...");
				System.out.println("----------------------------------------------------");
				wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		curnum += neednum;
		System.out.println(Thread.currentThread().getName() + ":生产了：" + neednum + ",剩余库存：" + curnum);
		notifyAll();
	}
	
	public synchronized void consume(int neednum) {
		while (neednum > curnum) {
			try {
				System.out.println("----------------------------------------------------");
				System.out.println(Thread.currentThread().getName() + "需要消费：" + neednum);
				System.out.println( "当前库存：" + curnum + ",库存不足，等待生产者生产");
				System.out.println("----------------------------------------------------");
				wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		curnum -= neednum;
		System.out.println(Thread.currentThread().getName() + ":已经消费了：" + neednum + ",还剩下库存量：" + curnum);
		notifyAll();
	}
}

class Producer implements Runnable {

	private Godown godown;
	
	private int neednum;
	
	public Producer(Godown godown,int neednum) {
		this.godown = godown;
		this.neednum = neednum;
	}
	@Override
	public void run() {
		godown.produce(neednum);
	}
}

class Consumer implements Runnable {

	private Godown godown;
	
	private int neednum;
	
	public Consumer(Godown godown,int neednum) {
		this.godown = godown;
		this.neednum = neednum;
	}
	@Override
	public void run() {
		godown.consume(neednum);
	}
}