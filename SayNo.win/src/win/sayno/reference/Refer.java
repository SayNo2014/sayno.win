package win.sayno.reference;

public class Refer {
	
	@Override
	protected void finalize() throws Throwable {
		System.out.println("对象被销毁...");
	}
}
