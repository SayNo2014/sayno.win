package win.sayno.string;

import java.lang.reflect.Field;

public class StringDemo_Back {
	int a = 9;
	public static void main(StringDemo_Back[] args) {
		String a = "sayno";
		String b = "sayno";
		String c = new String("sayno");
		try {
			System.out.println(a=="say" + "no");
			Field field = String.class.getDeclaredField("value");
			field.setAccessible(true);
			// 获取字符串的char数组
			char[] values = (char[]) field.get(c);
			System.out.println(field.get(c)); // 执行结果可以看出是数组
			values[3] = '_';
			System.out.println("a : " + a);
			System.out.println("b : " + b);
			System.out.println("c : " + c);
			System.out.println(a == "sayn_o");
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}
}
