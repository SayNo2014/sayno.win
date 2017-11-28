/*
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */

/*
 *
 *
 *
 *
 *
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

package java.util.concurrent.locks;

@SuppressWarnings("unused")
public class LockSupport {
	
	/**
	 * LockSupport���캯��
	 */
    private LockSupport() {}

    private static void setBlocker(Thread t, Object arg) {
    	// �����߳�t��parkBlockerΪarg
        UNSAFE.putObject(t, parkBlockerOffset, arg);
    }

    /**
     * �ͷ��߳�thread�����,���thread������״̬
     * @param thread
     */
    public static void unpark(Thread thread) {
        if (thread != null)
            UNSAFE.unpark(thread);
    }

    public static void park(Object blocker) {
        Thread t = Thread.currentThread();
        // ��¼��ǰ�̵߳ȴ��Ķ���
        setBlocker(t, blocker);
        // ��ȡ��ǰ�߳���ɣ�Ĭ�ϲ���ɣ�����ȡ������ɽ�һֱ����
        UNSAFE.park(false, 0L);
        // �������ִ�е�����ط�˵�������Ѿ����,�Ϳ��Խ�blocker����Ϊ��
        // ����ͨ��getBlocker������ȡ��blocker���п����Ǹ��߳���һ���������õ�blocker
        setBlocker(t, null);
    }

    /**
     * �����߳�,�������������ʱ��
     * @param blocker
     * @param nanos
     */
    public static void parkNanos(Object blocker, long nanos) {
        if (nanos > 0) {
            Thread t = Thread.currentThread();
            setBlocker(t, blocker);
            // false ����nanosΪ���ʱ��
            UNSAFE.park(false, nanos);
            setBlocker(t, null);
        }
    }

    public static void parkUntil(Object blocker, long deadline) {
        Thread t = Thread.currentThread();
        setBlocker(t, blocker);
        // true����deadlineΪ����ʱ��
        UNSAFE.park(true, deadline);
        setBlocker(t, null);
    }

    /**
     * ��ȡ�߳�t�����Ķ���parkBlocker
     * @param t
     * @return
     */
    public static Object getBlocker(Thread t) {
        if (t == null)
            throw new NullPointerException();
        return UNSAFE.getObjectVolatile(t, parkBlockerOffset);
    }

    public static void park() {
    	// ��ȡ��ɣ�����ʱ��Ϊ���޳���ֱ�����Ի�ȡ���
        UNSAFE.park(false, 0L);
    }

    public static void parkNanos(long nanos) {
        if (nanos > 0)
            UNSAFE.park(false, nanos);
    }

    public static void parkUntil(long deadline) {
        UNSAFE.park(true, deadline);
    }

    static final int nextSecondarySeed() {
        int r;
        Thread t = Thread.currentThread();
        if ((r = UNSAFE.getInt(t, SECONDARY)) != 0) {
            r ^= r << 13;   // xorshift
            r ^= r >>> 17;
            r ^= r << 5;
        }
        else if ((r = java.util.concurrent.ThreadLocalRandom.current().nextInt()) == 0)
            r = 1; // avoid zero
        UNSAFE.putInt(t, SECONDARY, r);
        return r;
    }

    // ���������Ǹ���Unsafe�е�objectFieldOffset������ȡThread���Ե�ƫ����(��ַ)
    private static final sun.misc.Unsafe UNSAFE;
    private static final long parkBlockerOffset;
    private static final long SEED;
    private static final long PROBE;
    private static final long SECONDARY;
    static {
        try {
        	// ��ȡUNSAFE ʵ��
            UNSAFE = sun.misc.Unsafe.getUnsafe();
            Class<?> tk = Thread.class;
            // ��ȡThread��parkBlocker�ֶε��ڴ�ƫ�Ƶ�ַ
            parkBlockerOffset = UNSAFE.objectFieldOffset
                (tk.getDeclaredField("parkBlocker"));
            // ��ȡThread��threadLocalRandomSeed�ֶε��ڴ�ƫ�Ƶ�ַ
            SEED = UNSAFE.objectFieldOffset
                (tk.getDeclaredField("threadLocalRandomSeed"));
            // ��ȡThread��threadLocalRandomProbe�ֶε��ڴ�ƫ�Ƶ�ַ
            PROBE = UNSAFE.objectFieldOffset
                (tk.getDeclaredField("threadLocalRandomProbe"));
            // ��ȡThread��threadLocalRandomSecondarySeed�ֶε��ڴ�ƫ�Ƶ�ַ
            SECONDARY = UNSAFE.objectFieldOffset
                (tk.getDeclaredField("threadLocalRandomSecondarySeed"));
        } catch (Exception ex) { throw new Error(ex); }
    }

}
