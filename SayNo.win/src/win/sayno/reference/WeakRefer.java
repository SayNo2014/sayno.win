package win.sayno.reference;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;

public class WeakRefer {
	public static void main(String[] args) {
		Refer refer = new Refer();
		ReferenceQueue<Refer> dead = new ReferenceQueue<Refer>();
		WeakReference<Refer> weakReference = new WeakReference<Refer>(refer,dead);
		
		System.out.println("强引用存在的情况下进行gc...");
		SoftRefer.gc();
		System.out.println("referent:" + weakReference.get());
		System.out.println("dead：" + dead.poll());
		
		// WeakReference时不论内存足不足够,只要强引用除了被弱引用引用,其他地方没有被使用,就会被回收
		System.out.println("删除强引用...");
		refer = null;
		System.out.println("referent:" + weakReference.get());
		System.out.println("dead：" + dead.poll());
		
		System.out.println("删除强引用,并执行gc...");
		SoftRefer.gc();
		System.out.println("referent:" + weakReference.get());
		System.out.println("dead：" + dead.poll());
	}

	public static void gc() {
		System.out.println("准备GC...");
		System.gc();
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("GC结束...");
	}
}
