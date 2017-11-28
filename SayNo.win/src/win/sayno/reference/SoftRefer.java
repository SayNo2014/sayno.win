package win.sayno.reference;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.List;

public class SoftRefer {
	
	/**
	 * ������ѯ�ı���
	 */
	private static volatile boolean flag = true;
	
	private static volatile ReferenceQueue<Refer> dead;
	
	private static volatile SoftReference<Refer> softReference;
	
	public static void main(String[] args) {
		Refer refer = new Refer();
		dead = new ReferenceQueue<Refer>();
		softReference = new SoftReference<Refer>(refer,dead);
		
		new Thread(new Runnable() {
			@Override
			public void run() {
				while(true) {
					if (dead.poll() != null) {
						System.out.println("�����ƶ���ReferenceQueue...");
						System.out.println("referent:" + softReference.get());
						flag = false;
						break;
					}	
				}
			}}
		).start();
			
		System.out.println("gc֮ǰ...");
		System.out.println("referent:" + softReference.get());
		System.out.println("dead��" + dead.poll());
		
		SoftRefer.gc();
		System.out.println("gc֮��...");
		System.out.println("referent:" +
		
				
				softReference.get());
		System.out.println("dead��" + dead.poll());
		
		refer = null;
		SoftRefer.gc();
		System.out.println("ǿ����ɾ��,ִ��gc֮��...");
		System.out.println("referent:" + softReference.get());
		System.out.println("dead��" + dead.poll());
		
		System.out.println("���ڴ�ռ��...");
		List<Object> objects = new ArrayList<Object>(100000);
		try {
			while(flag) {
				objects.add(new Object());
			}
		} catch (OutOfMemoryError e) {
			System.out.println("�ڴ����...");
		}
		
		System.out.println("ǿ����ɾ��,�ڴ�Խ���...");
		System.out.println("referent:" + softReference.get());
		System.out.println("dead��" + dead.poll());
	}

	public static void gc() {
		System.out.println("׼��GC...");
		System.gc();
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("GC����...");
	}
}
