package win.sayno.operator;

public class Operator {

	public static void main(String[] args) {
		// λ��������һλ
		System.out.println(2>>1); // 1
		System.out.println(8>>2); // 2
		// λ�����޷���������λ
		System.out.println(16<<2); // 64
		// �޷���������λ
		System.out.println(16 >>> 2); // 4
		System.out.println(-16 >>> 2); // 1073741820 �޷�������,��λ����
		
		System.out.println((resizeStamp(1024)<<16) + 2);
		// Integer.numberOfLeadingZeros(n) | (1 << (RESIZE_STAMP_BITS - 1))
		
//		String[] strs = new String[12];
//		strs[0] = "hello";
//		System.out.println(args.length);
		int i = 2;
		if (i >0) {
			System.out.println("dfsa");
		} else if (i> 1) {
			System.out.println("dfsa");
		} else if (i==2) {
			System.out.println("dfsa");
		}
	}
	
	/**
	 * HashMap����ʱ�����㷨
	 * @param c
	 * @return
	 */
    private static final int tableSizeFor(int c) {
        int n = c - 1;
        n |= n >>> 1;
        n |= n >>> 2;
        n |= n >>> 4;
        n |= n >>> 8;
        n |= n >>> 16;
        return (n < 0) ? 1 : (n >= 16) ? 16 : n + 1;
    }
    
    static final int resizeStamp(int n) {
        return Integer.numberOfLeadingZeros(n) | (1 << (16 - 1));
    }
}
