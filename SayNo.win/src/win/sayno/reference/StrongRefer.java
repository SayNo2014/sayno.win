package win.sayno.reference;

public class StrongRefer {
	public static void main(String[] args) {
		Refer refer = new Refer();
		StrongRefer.gc();

		refer = null;
		StrongRefer.gc();
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
