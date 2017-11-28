package win.sayno.string;

public class Main {
	
	public static void main(String[] args) {
		System.out.println(Main.class.getClassLoader().getParent().getParent());
	}
	
	public static void updateUser(User user) {
		System.out.println(user.hashCode());
		user.setName("sayno");
	}
}
