package win.sayno.dynamic.jdk;

public class Main {
	public static void main(String[] args) {
		MyHandler handler = new MyHandler();
		handler.bind(new Target());
		((TargetInterface)handler.getProxy()).doSomething();
	}
}
