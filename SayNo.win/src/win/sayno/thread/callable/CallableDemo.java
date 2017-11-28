package win.sayno.thread.callable;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class CallableDemo {
	public static void main(String[] args) {
		Callable<String> c1 = new MyThread();
		Callable<String> c2 = new MyThread();
		Callable<String> c3 = new MyThread();
		ExecutorService pool = Executors.newFixedThreadPool(3);
		Future<String> f1 = pool.submit(c1);
		Future<String> f2 = pool.submit(c2);
		Future<String> f3 = pool.submit(c3);
		try {
			System.out.println("c1:" + f1.get().toString());
			System.out.println("c2:" + f2.get().toString());
			System.out.println("c3:" + f3.get().toString());
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		} finally {
			pool.shutdown();
		}
	}
}

class MyThread implements Callable<String> {

	@Override
	public String call() throws Exception {
		return Thread.currentThread().getName() + "sayno";
	}
	
} 
