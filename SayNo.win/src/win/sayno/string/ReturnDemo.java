package win.sayno.string;

public class ReturnDemo {
	public static void main(String[] args) {
		String s1 = "a";
		String s2 = s1 + "b";
		String s3 = "a" + "b";
		System.out.println(s2 == "ab");
		System.out.println(s3 == "ab");
	}
	
	public static int test(){
		int i = 0;
		try {
			return i;
		} catch (Exception e) {
			
		} finally {
			i++;
		}
		return i;
	}
}
