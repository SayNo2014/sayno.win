package win.sayno.reference;

import java.lang.ref.PhantomReference;
import java.lang.ref.ReferenceQueue;

public class PhantomRefer {
	public static void main(String[] args) {
		CunstomPhantomRefer refer = new CunstomPhantomRefer();
		ReferenceQueue<CunstomPhantomRefer> dead = new ReferenceQueue<CunstomPhantomRefer>();
		PhantomReference<CunstomPhantomRefer> phantomReference = new PhantomReference<CunstomPhantomRefer>(refer, dead);
		System.out.println("referent:" + phantomReference.get());
		System.out.println("dead£º" + dead.poll());
		
		SoftRefer.gc();

		refer = null;
		System.out.println("referent:" + phantomReference.get());
		System.out.println("dead£º" + dead.poll());
		
		SoftRefer.gc();
		System.out.println("referent:" + phantomReference.get());
		System.out.println("dead£º" + dead.poll());
	}

	public static void gc() {
		System.out.println("×¼±¸GC...");
		System.gc();
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("GC½áÊø...");
	}
}
class CunstomPhantomRefer {
}
