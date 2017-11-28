package win.sayno.thread.threadlocal;

public class MyThread implements Runnable {

	private MyThreadLocal myThreadLocal = new MyThreadLocal();
	
	@Override
	public void run() {
		myThreadLocal.getNum();
		System.out.println(myThreadLocal.getCount().get());
	}
	
	public MyThreadLocal getMyThreadLocal() {
		return myThreadLocal;
	}

	public void setMyThreadLocal(MyThreadLocal myThreadLocal) {
		this.myThreadLocal = myThreadLocal;
	}
	
}
