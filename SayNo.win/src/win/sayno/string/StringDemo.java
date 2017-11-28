package win.sayno.string;

public class StringDemo {
	int a = 0;
	
	public static void main(String[] args) throws Exception {
		
		String s1 = "Hello";
		String s2 = "Hello";
		String s3 = "Hel" + "lo";
		String s4 = "Hel" + new String("lo");
		String s5 = new String("Hello");
		String s6 = s5.intern();
		String s7 = "H";
		String s8 = "ello";
		String s9 = s7 + s8;
		String s10 = new StringBuilder("Hel").append("lo").toString();
		          
		System.out.println(s1 == s2);  // true
		System.out.println(s1 == s3);  // true
		System.out.println(s1 == s4);  // false
		System.out.println(s1 == s5);  // false
		System.out.println(s1 == s9);  // false
		System.out.println(s4 == s5);  // false
		System.out.println(s1 == s6);  // true
		System.out.println(s10 == s4);  // false
		System.out.println(s4.intern() == s1);
		
		switch (4) {  
		case 1:  
            System.out.println(1);
        case 2:  
            System.out.println(2);
        case 3:  
            System.out.println(3);
        default:  
            System.out.println(4);
            break;
		}
	}
}
