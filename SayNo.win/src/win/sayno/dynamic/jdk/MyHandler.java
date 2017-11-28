package win.sayno.dynamic.jdk;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class MyHandler implements InvocationHandler {
	
	private TargetInterface target;
	
	public void bind(TargetInterface target) {
		this.target = target;
	}
	
	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		System.out.println("日志开始...");
		Object obj = method.invoke(target, args);
		System.out.println("日志结束...");
		return obj;
	}

	public Object getProxy() {
		return Proxy.newProxyInstance(
				this.getClass().getClassLoader(), target.getClass().getInterfaces(), this);
	}
}
