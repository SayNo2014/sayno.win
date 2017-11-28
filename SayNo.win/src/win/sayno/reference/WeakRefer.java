package win.sayno.reference;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;

public class WeakRefer {
	public static void main(String[] args) {
		Refer refer = new Refer();
		ReferenceQueue<Refer> dead = new ReferenceQueue<Refer>();
		WeakReference<Refer> weakReference = new WeakReference<Refer>(refer,dead);
		
		System.out.println("ǿ���ô��ڵ�����½���gc...");
		SoftRefer.gc();
		System.out.println("referent:" + weakReference.get());
		System.out.println("dead��" + dead.poll());
		
		// WeakReferenceʱ�����ڴ��㲻�㹻,ֻҪǿ���ó��˱�����������,�����ط�û�б�ʹ��,�ͻᱻ����
		System.out.println("ɾ��ǿ����...");
		refer = null;
		System.out.println("referent:" + weakReference.get());
		System.out.println("dead��" + dead.poll());
		
		System.out.println("ɾ��ǿ����,��ִ��gc...");
		SoftRefer.gc();
		System.out.println("referent:" + weakReference.get());
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
