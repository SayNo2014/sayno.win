package win.sayno.thread.threadlocal;

public class MyThreadLocal {
	private ThreadLocal<Integer> count = new ThreadLocal<Integer>() {
		@Override
		protected Integer initialValue() {
			return 0;
		};
	};
	
	public void getNum(){
		count.set(count.get() + 1);
	}

	public ThreadLocal<Integer> getCount() {
		return count;
	}

	public void setCount(ThreadLocal<Integer> count) {
		this.count = count;
	}
	
}
