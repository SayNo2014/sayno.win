package win.sayno.thread.semaphore;

import java.util.concurrent.Semaphore;

public class SemaphoreTest {
	public static void main(String[] args) {
		// ͣ�����������λ
		Semaphore semaphore = new Semaphore(5);
		// ʮ������ͣ������
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
					+ " ����ͣ����");
			int time = (int) (Math.random()*10);
			Thread.sleep(time*1000);
			System.out.println(Thread.currentThread().getName()
					+ " �뿪ͣ����,ͣ��ʱ�䣺" + time + "��");
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			this.semaphore.release();
		}
	}
}

