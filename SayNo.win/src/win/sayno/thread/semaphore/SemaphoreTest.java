package win.sayno.thread.semaphore;

import java.util.concurrent.Semaphore;

public class SemaphoreTest {
	public static void main(String[] args) {
		// 停车场有五个车位
		Semaphore semaphore = new Semaphore(5);
		// 十辆车有停车需求
		for (int i=0;i< 10;i++) {
			new Thread(new Car(semaphore)).start();;
		}
	}
}

class Car implements Runnable {
	
	Semaphore semaphore = null; 
	
	public Car(Semaphore semaphore) {
		this.semaphore = semaphore;
	}
	
	@Override
	public void run() {
		try {
			this.semaphore.acquire();
			System.out.println(Thread.currentThread().getName()
					+ " 进入停车场");
			int time = (int) (Math.random()*10);
			Thread.sleep(time*1000);
			System.out.println(Thread.currentThread().getName()
					+ " 离开停车场,停车时间：" + time + "秒");
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			this.semaphore.release();
		}
	}
}

