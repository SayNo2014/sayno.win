package win.sayno.cglib;

import org.junit.Test;

import net.sf.cglib.core.DebuggingClassWriter;
import net.sf.cglib.proxy.Enhancer;

public class DynamicProxy {

	@Test
	public void testProxy() {
		// 设置DEBUG_LOCATION_PROPERTY属性指定代理类class文件生成路径
		// 目录该class只保存在内存中
		System.setProperty(DebuggingClassWriter.DEBUG_LOCATION_PROPERTY,
				"C:\\\\Users\\\\SayNo\\\\Desktop\\\\Proxy$0.class");
		// 实例化增强器，cglib中的class generator
		Enhancer enhancer = new Enhancer();
		
		// 设置目标类
		enhancer.setSuperclass(RealSubject.class);
		
		// 设置拦截对象
		enhancer.setCallback(new Interceptor());
		
		// 生成代理类
		RealSubject realSubject = (RealSubject) enhancer.create();
		
		realSubject.doSomething();
	} 
}
