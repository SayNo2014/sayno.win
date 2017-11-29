package win.sayno.cglib;

import org.junit.Test;

import net.sf.cglib.core.DebuggingClassWriter;
import net.sf.cglib.proxy.Enhancer;

public class DynamicProxy {

	@Test
	public void testProxy() {
		// ����DEBUG_LOCATION_PROPERTY����ָ��������class�ļ�����·��
		// Ŀ¼��classֻ�������ڴ���
		System.setProperty(DebuggingClassWriter.DEBUG_LOCATION_PROPERTY,
				"C:\\\\Users\\\\SayNo\\\\Desktop\\\\Proxy$0.class");
		// ʵ������ǿ����cglib�е�class generator
		Enhancer enhancer = new Enhancer();
		
		// ����Ŀ����
		enhancer.setSuperclass(RealSubject.class);
		
		// �������ض���
		enhancer.setCallback(new Interceptor());
		
		// ���ɴ�����
		RealSubject realSubject = (RealSubject) enhancer.create();
		
		realSubject.doSomething();
	} 
}
