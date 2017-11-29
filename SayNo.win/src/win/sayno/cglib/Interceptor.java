package win.sayno.cglib;

import java.lang.reflect.Method;

import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

public class Interceptor implements MethodInterceptor {

	@Override
	public Object intercept(Object arg0, Method arg1,
			Object[] arg2, MethodProxy arg3) throws Throwable {
		System.out.println("代理开始...");
		arg3.invokeSuper(arg0, arg2);
		System.out.println("代理结束...");
		return null;
	}
}
