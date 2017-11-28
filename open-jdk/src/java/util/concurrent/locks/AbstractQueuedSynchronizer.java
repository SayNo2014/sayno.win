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
import java.util.concurrent.TimeUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import sun.misc.Unsafe;

public abstract class AbstractQueuedSynchronizer
    extends AbstractOwnableSynchronizer
    implements java.io.Serializable {

    private static final long serialVersionUID = 7373984972572414691L;

    /**
     * Creates a new {@code AbstractQueuedSynchronizer} instance
     * with initial synchronization state of zero.
     */
    protected AbstractQueuedSynchronizer() { }

    /**
     * �ڵ�ʱ����ͬ�����еĻ���
     * û�гɹ���ȡ���Ľڵ㽫����ӵ����е�β�����Ӷ����л����Ǵ�ͷ����ȡ�ڵ�
     * @author SayNo
     *
     */
    static final class Node {
        /** Marker to indicate a node is waiting in shared mode */
        static final Node SHARED = new Node();
        /** Marker to indicate a node is waiting in exclusive mode */
        static final Node EXCLUSIVE = null;

        /**
         * ͬ�������еȴ����̵߳ȴ���ʱ���߱��жϣ���Ҫ��ͬ��������ȡ���ȴ�
         */
        static final int CANCELLED =  1;
        
        /**
         * ���Ѻ�̽ڵ�
         */
        static final int SIGNAL    = -1;
       
        /**
         * �ڵ��ڵȴ������У��ڵ��̵߳ȴ������Condition��
         * �������̶߳����Condition�������signal()\signalAll()
         * ������ڵ㽫����ȴ�����������ͬ�������У�׼�����Ի�ȡͬ��״̬
         */
        static final int CONDITION = -2;

        /**
         * ������
         */
        static final int PROPAGATE = -3;

        /**
         * �߳̽ڵ�ĵȴ�״̬,waitStatus��ʼ״̬Ϊ0
         * ֵ�ο�CANCELLED,SIGNAL,CONDITION,PROPAGATE
         */
        volatile int waitStatus;

        /**
         * ǰ���ڵ�
         */
        volatile Node prev;

        /**
         * ��̽ڵ�
         */
        volatile Node next;

        /**
         * �ڵ������߳�
         */
        volatile Thread thread;

        /**
         * �ȴ����еĺ�̽ڵ�
         */
        Node nextWaiter;

        /**
         * Returns true if node is waiting in shared mode.
         */
        final boolean isShared() {
            return nextWaiter == SHARED;
        }

        /**
         * 	��ȡǰ���ڵ�
         *	ģ�巽��,������ʵ��
         */
        final Node predecessor() throws NullPointerException {
            Node p = prev;
            if (p == null)
                throw new NullPointerException();
            else
                return p;
        }

        Node() {
        }

        /**
         * Used by addWaiter
         * @param thread ��ǰ�߳�
         * @param mode �ڵ�ģʽ
         */
        Node(Thread thread, Node mode) {     
            this.nextWaiter = mode;
            this.thread = thread;
        }

        /**
         * Used by Condition
         * @param thread ��ǰ�߳�
         * @param waitStatus �ȴ�״̬
         */
        Node(Thread thread, int waitStatus) {
            this.waitStatus = waitStatus;
            this.thread = thread;
        }
    }

    /**
     * Head of the wait queue, lazily initialized.  Except for
     * initialization, it is modified only via method setHead.  Note:
     * If head exists, its waitStatus is guaranteed not to be
     * CANCELLED.
     */
    private transient volatile Node head;

    /**
     * Tail of the wait queue, lazily initialized.  Modified only via
     * method enq to add new wait node.
     */
    private transient volatile Node tail;

    /**
     * The synchronization state.
     */
    private volatile int state;

    /**
     * Returns the current value of synchronization state.
     * This operation has memory semantics of a {@code volatile} read.
     * @return current state value
     */
    protected final int getState() {
        return state;
    }

    /**
     * Sets the value of synchronization state.
     * This operation has memory semantics of a {@code volatile} write.
     * @param newState the new state value
     */
    protected final void setState(int newState) {
        state = newState;
    }

    /**
     * Atomically sets synchronization state to the given updated
     * value if the current state value equals the expected value.
     * This operation has memory semantics of a {@code volatile} read
     * and write.
     *
     * @param expect the expected value
     * @param update the new value
     * @return {@code true} if successful. False return indicates that the actual
     *         value was not equal to the expected value.
     */
    protected final boolean compareAndSetState(int expect, int update) {
        // See below for intrinsics setup to support this
        return unsafe.compareAndSwapInt(this, stateOffset, expect, update);
    }

    // Queuing utilities

    /**
     * The number of nanoseconds for which it is faster to spin
     * rather than to use timed park. A rough estimate suffices
     * to improve responsiveness with very short timeouts.
     */
    static final long spinForTimeoutThreshold = 1000L;

    /**
     * Inserts node into queue, initializing if necessary. See picture above.
     * @param node the node to insert
     * @return node's predecessor
     */
    private Node enq(final Node node) {
    	// ����
        for (;;) {
            Node t = tail;
            if (t == null) {
            	// CAS���ж϶����Ƿ�Ϊ��
                if (compareAndSetHead(new Node()))
                	// ���ö���ͷ = ����β
                    tail = head;
            } else {
            	// β�ڵ㲻Ϊ�գ�������Ľڵ��β�ڵ��
                node.prev = t;
                if (compareAndSetTail(t, node)) {
                    t.next = node;
                    return t;
                }
            }
        }
    }

    /**
     * ���ݴ���Ľڵ�ģʽ�����Ŷӽڵ�
     * ReentrantLock ��ռ��
     * ReadWriteLock ������
     *
     * @param mode Node.EXCLUSIVE ��ռ��, Node.SHARED ������
     * @return the new node
     */
    private Node addWaiter(Node mode) {
    	// �����½ڵ�
        Node node = new Node(Thread.currentThread(), mode);
        // ǳ����β�ڵ�
        Node pred = tail;
        if (pred != null) {
        	// β�ڵ㲻Ϊ��ʱ�������½ڵ��ǰ�ڵ�Ϊβ�ڵ�
            node.prev = pred;
            // ִ��CASԭ�Ӳ�����ԭֵΪ��ǰ�����β�ڵ��ƫ�Ƶ�ַ��ֵ,����ֵΪtail,����ֵΪnode
            if (compareAndSetTail(pred, node)) {
            	// �½ڵ�����Ϊԭβ�ڵ�ĺ�һ�ڵ�
                pred.next = node;
                // �����½ڵ�
                return node;
            }
        }
        // ���½ڵ���ӵ����еĶ�β
        enq(node);
        return node;
    }

    /**
     * Sets head of queue to be node, thus dequeuing. Called only by
     * acquire methods.  Also nulls out unused fields for sake of GC
     * and to suppress unnecessary signals and traversals.
     *
     * @param node the node
     */
    private void setHead(Node node) {
        head = node;
        node.thread = null;
        node.prev = null;
    }

    /**
     * ��ɺ����ڵ�
     *
     * @param node the node
     */
    private void unparkSuccessor(Node node) {
    	// ��ȡ��ǰ�ڵ��waitStatus
        int ws = node.waitStatus;
        if (ws < 0)
        	// ״ֵ̬С��0��ΪSIGNAL -1 �� CONDITION -2 �� PROPAGATE -3
        	// �Ƚϲ������ýڵ�ȴ�״̬������Ϊ0
            compareAndSetWaitStatus(node, ws, 0);

        // sΪnode�ĺ�̽ڵ�
        Node s = node.next;
        if (s == null || s.waitStatus > 0) {
        	// ��һ���ڵ�Ϊ�ջ�����һ���ڵ�ĵȴ�״̬����0����ΪCANCELLED
            s = null;
            // ��ȡ����ǰ�ڵ�ĺ�̽ڵ��е�һ���߳�û��ȡ���Ľڵ�
            for (Node t = tail; t != null && t != node; t = t.prev)
                if (t.waitStatus <= 0)
                    s = t;
        }
        if (s != null)
            LockSupport.unpark(s.thread);
    }

    /**
     * Release action for shared mode -- signals successor and ensures
     * propagation. (Note: For exclusive mode, release just amounts
     * to calling unparkSuccessor of head if it needs signal.)
     */
    private void doReleaseShared() {
        for (;;) {
        	// 1. h==null��˵����ʱͬ������Ϊ�գ�û�к�̽ڵ㣬Ҳ��û���߳̿ɹ�����
        	// 2. h!=null && h==tail����2�����������û�пɹ����ѵĽڵ�
        	// 2.1. head==tail==new Node()��ͬ�����е�tailΪnullʱ��ӣ�
        	// ��ʼ���Ŀսڵ�new Node()����������뿴AQS.enq()
        	// 2.2 ͬ������β�ڵ㱻���ѣ���ʱhead==tail����������뿴
        	// AQS.doAcquireSharedInterruptibly
            Node h = head;
            if (h != null && h != tail) {
                int ws = h.waitStatus;
                if (ws == Node.SIGNAL) {
                    if (!compareAndSetWaitStatus(h, Node.SIGNAL, 0))
                    	// ������ if(h==head) �ķ��������ܴ��ڶ���̲߳�����ִ��doReleaseShared
                    	// ��������CAS�������п��ܻ�ʧ�ܣ�������һ�ֻ��Ѿ���
                        continue;
                    // ����ʱֻ��һ���߳���CAS�����ɹ���head.waitStatus:Node.SIGNAL->0������ͷ���ĺ�̽ڵ�
                    unparkSuccessor(h);
                }
                else if (ws == 0 &&
                         !compareAndSetWaitStatus(h, 0, Node.PROPAGATE))
                	// ������ if(h==head) �ķ��������ܴ��ڶ���̲߳�����ִ��doReleaseShared
                	// ��������CAS�������п��ܻ�ʧ�ܣ�������һ�ֻ��Ѿ���
                	// �����뻽�Ѿ������̺߳ܶ�ʱ�����п��ܻᵼ��ws=PROPAGATE
                	// ��ws=PROPAGATE�ֻᵼ���̻߳�����ж��Ƿ�������뻽�Ѿ���
                	// ��������뻽�Ѿ����̻߳ᷢ��h==head���˳����Ѿ���
                	continue;                // loop on failed CAS
            }
            // ִ�е����˵����Ȼ�Ѿ����߳��Ѿ�ִ����unparkSuccessor(h)����
            // ����һ�ֻ��Ѿ�����Ȼ��ʼ
            // ��һ������Ҫ�жϵ�ǰ�߳��Ƿ�Ҫ������һ�ֵĻ��Ѿ���
            if (h == head)                   // loop if head changed
                break;
        }
    }

    /**
     * Sets head of queue, and checks if successor may be waiting
     * in shared mode, if so propagating if either propagate > 0 or
     * PROPAGATE status was set.
     *
     * @param node the node
     * @param propagate the return value from a tryAcquireShared
     */
    private void setHeadAndPropagate(Node node, int propagate) {
        Node h = head; 
        setHead(node);
        if (propagate > 0 || h == null || h.waitStatus < 0 ||
            (h = head) == null || h.waitStatus < 0) {
            Node s = node.next;
            if (s == null || s.isShared())
                doReleaseShared();
        }
    }


    /**
     * Cancels an ongoing attempt to acquire.
     *
     * @param node the node
     */
    private void cancelAcquire(Node node) {
        // ��ǰ�ڵ�Ϊ��,�����κβ���
        if (node == null)
            return;

        // ���ڵ�ǰ�߳��ж�,ֱ�ӽ���ǰ�̶߳�Ӧ�Ľڵ���Ϊ��
        node.thread = null;

        // ���õ�ǰ�ڵ��ǰ���ڵ�Ϊpred
        Node pred = node.prev;
        while (pred.waitStatus > 0)
        	// ���еݹ�ɾ����ȡ�����߳�
            node.prev = pred = pred.prev;

        // ����ǰ���ڵ�ĺ�̽ڵ�ΪpredNext
        Node predNext = pred.next;

        // ����node�ڵ��״̬Ϊȡ����CANCELLED)
        node.waitStatus = Node.CANCELLED;

		// ����ǰ�ڵ�Ϊͬ������β�ڵ�,CAS����ͬ������β�ڵ�Ϊ��ǰ�ڵ��ǰ���ڵ�
		if (node == tail && compareAndSetTail(node, pred)) {
		    compareAndSetNext(pred, predNext, null);
		} else {
			// ִ�е��˴�,˵����ǰ�ڵ���ͬ�������м���ǰ���ڵ�Ҳ���ں�̽ڵ�
		    int ws;
		    if (pred != head &&
		        ((ws = pred.waitStatus) == Node.SIGNAL ||
		         (ws <= 0 && compareAndSetWaitStatus(pred, ws, Node.SIGNAL))) &&
		        pred.thread != null) {
		    	// �����ǰ�ڵ��ǰ���ڵ㲻Ϊͷ����ҵ�ǰ�ڵ��ǰ���ڵ��ǿɻ���״̬,����ǰ�ڵ�ĺ�̽ڵ㴮���ĵ�ǰ�ڵ��
		    	// ǰ���ڵ��,ɾ����ǰ�ڵ�
		    	// �����ǰ�ڵ��ǰ���ڵ㲻Ϊͷ����ҵ�ǰ�ڵ��ǰ���ڵ�Ϊ�ǻ���״̬,���ó��Ը���ǰ���ڵ�״̬Ϊ�ɻ���״̬
		    	// �����³ɹ�,������ǰ��ǰ���ڵ�ͺ�̽ڵ�,ɾ����ǰ�ڵ�
		        Node next = node.next;
		        if (next != null && next.waitStatus <= 0)
		            compareAndSetNext(pred, predNext, next);
		    } else {
		    	// 1.�����ǰ�ڵ�Ϊhead�ڵ�,ֱ�ӿ��Ի��Ѻ�̽ڵ�
		    	// 2.�����ǰ�ڵ㲻Ϊhead�ڵ��Һ�̽ڵ�״̬Ϊ�ǻ���״̬,���Ը���ǰ���ڵ�״̬Ϊ�ɻ���״̬ʧ��,�������
		    	// ��Ϊ��ǰ�ڵ��ǰ���ڵ��Ѿ���ȡ��,unparkSuccessor���������ȡ���ڵ�,������Ч�߳�
		        unparkSuccessor(node);
		    }
		
		    // �ӿ���������
		    node.next = node;
		}
    }

    private static boolean shouldParkAfterFailedAcquire(Node pred, Node node) {
        int ws = pred.waitStatus;
        if (ws == Node.SIGNAL)
        	// ��ǰ�ڵ��ǰ���ڵ�ΪSIGNAL״̬,����������SIGNAL�ĺ���Ϊ��ǰ�ڵ�ĺ�̽ڵ������߳���Ҫ���У�
        	// ����true������ǰ�ڵ����park
            return true;
        if (ws > 0) {
        	// ��ǰ�ڵ��ǰ���ڵ��߳��ѱ�ȡ��
        	// ʹ��do-while ��ͬ���������Ƴ����cancelled��ǰ���ڵ�,������ǰ����
        	// ֱ���ҵ�һ����canncelled�Ľڵ��,������Ϊ�Լ���ǰ���ڵ㡣
            do {
                node.prev = pred = pred.prev;
            } while (pred.waitStatus > 0);
            pred.next = node;
        } else {
        	// ����ǰ���ڵ��״̬ΪNode.SIGNAL
            compareAndSetWaitStatus(pred, ws, Node.SIGNAL);
        }
        return false;
    }

    /**
     * Convenience method to interrupt current thread.
     */
    static void selfInterrupt() {
        Thread.currentThread().interrupt();
    }

    /**
     * Convenience method to park and then check if interrupted
     *
     * @return {@code true} if interrupted
     */
    private final boolean parkAndCheckInterrupt() {
    	// ����park����������ǰ�߳�,ֱ���Լ���ǰ���ڵ��ͷ������Լ�����
        LockSupport.park(this);
        // �����߳��Ƿ��Ѿ����ж�
        return Thread.interrupted();
    }

    /*
     * Various flavors of acquire, varying in exclusive/shared and
     * control modes.  Each is mostly the same, but annoyingly
     * different.  Only a little bit of factoring is possible due to
     * interactions of exception mechanics (including ensuring that we
     * cancel if tryAcquire throws exception) and other control, at
     * least not without hurting performance too much.
     */

    /**
     * Acquires in exclusive uninterruptible mode for thread already in
     * queue. Used by condition wait methods as well as acquire.
     *
     * @param node the node
     * @param arg the acquire argument
     * @return {@code true} if interrupted while waiting
     */
    final boolean acquireQueued(final Node node, int arg) {
        boolean failed = true;
        try {
        	// �߳��жϱ�ʶ
            boolean interrupted = false;
            // �����Գ��Ի�ȡ����ֱ������node��ǰ����ͷ�ڵ㲢��node��ȡ״̬�ɹ�,���ͷ�ͷ�ڵ�
            for (;;) {
            	// node addWaiter��ӵĵȴ��߳�
            	// ��ȡ��ǰ�ڵ�(sync��β�ڵ�)��ǰ���ڵ�
                final Node p = node.predecessor();
                // �����һ���ڵ�Ϊͷ�ڵ�,�����Ƿ�ɹ���ȡ����
                if (p == head && tryAcquire(arg)) {
                	// ���ͷ�ڵ��ȡ����������head�ĺ�̽ڵ�pΪͷ�ڵ�
                    setHead(node);
                    // ����head��nextΪnull��gc����p�ڵ�
                    p.next = null;
                    failed = false;
                    // ����������ԭ��ͷ�ڵ㱻ɾ��
                    return interrupted;
                }
                if (shouldParkAfterFailedAcquire(p, node) &&
                    parkAndCheckInterrupt())
                	// �����жϱ�ʶΪtrue
                    interrupted = true;
            }
        } finally {
        	// ��������쳣,try catch�����û���������أ�ɾ���ڵ������Դ
            if (failed)
                cancelAcquire(node);
        }
    }

    private void doAcquireInterruptibly(int arg)
        throws InterruptedException {
    	// ��ȡ��ʧ��,������ǰ�̶߳�Ӧ��ͬ�����нڵ�
        final Node node = addWaiter(Node.EXCLUSIVE);
        boolean failed = true;
        try {
            for (;;) {
                final Node p = node.predecessor();
                if (p == head && tryAcquire(arg)) {
                    setHead(node);
                    p.next = null;
                    failed = false;
                    return;
                }
                if (shouldParkAfterFailedAcquire(p, node) &&
                    parkAndCheckInterrupt())
                	// ����߳���park�����̱߳��ж϶��������,���׳�InterruptedException
                    throw new InterruptedException();
            }
        } finally {
            if (failed)
                cancelAcquire(node);
        }
    }

    /**
     * Acquires in exclusive timed mode.
     *
     * @param arg the acquire argument
     * @param nanosTimeout max wait time
     * @return {@code true} if acquired
     */
    private boolean doAcquireNanos(int arg, long nanosTimeout)
            throws InterruptedException {
        if (nanosTimeout <= 0L)
            return false;
        final long deadline = System.nanoTime() + nanosTimeout;
        final Node node = addWaiter(Node.EXCLUSIVE);
        boolean failed = true;
        try {
            for (;;) {
                final Node p = node.predecessor();
                if (p == head && tryAcquire(arg)) {
                    setHead(node);
                    p.next = null; // help GC
                    failed = false;
                    return true;
                }
                nanosTimeout = deadline - System.nanoTime();
                if (nanosTimeout <= 0L)
                	// ��ʱ��ִ��cancelAcquire��ȡ����������Ȼ�󷵻�false
                    return false;
                // ʣ������ʱ��ֻʣ1000����ʱ������߳�����
                if (shouldParkAfterFailedAcquire(p, node) &&
                    nanosTimeout > spinForTimeoutThreshold)
                    LockSupport.parkNanos(this, nanosTimeout);
                if (Thread.interrupted())
                    throw new InterruptedException();
            }
        } finally {
            if (failed)
                cancelAcquire(node);
        }
    }

    /**
     * Acquires in shared uninterruptible mode.
     * @param arg the acquire argument
     */
    private void doAcquireShared(int arg) {
        final Node node = addWaiter(Node.SHARED);
        boolean failed = true;
        try {
            boolean interrupted = false;
            for (;;) {
                final Node p = node.predecessor();
                if (p == head) {
                    int r = tryAcquireShared(arg);
                    if (r >= 0) {
                        setHeadAndPropagate(node, r);
                        p.next = null; // help GC
                        if (interrupted)
                            selfInterrupt();
                        failed = false;
                        return;
                    }
                }
                if (shouldParkAfterFailedAcquire(p, node) &&
                    parkAndCheckInterrupt())
                    interrupted = true;
            }
        } finally {
            if (failed)
                cancelAcquire(node);
        }
    }

    /**
     * Acquires in shared interruptible mode.
     * @param arg the acquire argument
     */
    private void doAcquireSharedInterruptibly(int arg)
        throws InterruptedException {
    	// CountDownLatch���õ���AQS�Ĺ���ģʽ����ReentrantLock���õ���AQS�Ķ�ռģʽ
        final Node node = addWaiter(Node.SHARED);
        boolean failed = true;
        try {
            for (;;) {
                final Node p = node.predecessor();
                if (p == head) {
                	// ���Ի�ȡ������
                    int r = tryAcquireShared(arg);
                    if (r >= 0) {
                    	// ��ǰ�ڵ��ǰ���ڵ�Ϊhead��r>=0
                        setHeadAndPropagate(node, r);
                        p.next = null;
                        failed = false;
                        return;
                    }
                }
                // �����ǰ�ڵ��ǰ���ڵ㲻��ͷ�ڵ㣬����state!=0�����Թ����̲߳��ȴ������ѻ��ж�
                if (shouldParkAfterFailedAcquire(p, node) &&
                    parkAndCheckInterrupt())
                    throw new InterruptedException();
            }
        } finally {
            if (failed)
                cancelAcquire(node);
        }
    }

    /**
     * Acquires in shared timed mode.
     *
     * @param arg the acquire argument
     * @param nanosTimeout max wait time
     * @return {@code true} if acquired
     */
    private boolean doAcquireSharedNanos(int arg, long nanosTimeout)
            throws InterruptedException {
        if (nanosTimeout <= 0L)
            return false;
        final long deadline = System.nanoTime() + nanosTimeout;
        final Node node = addWaiter(Node.SHARED);
        boolean failed = true;
        try {
            for (;;) {
                final Node p = node.predecessor();
                if (p == head) {
                    int r = tryAcquireShared(arg);
                    if (r >= 0) {
                        setHeadAndPropagate(node, r);
                        p.next = null; // help GC
                        failed = false;
                        return true;
                    }
                }
                nanosTimeout = deadline - System.nanoTime();
                if (nanosTimeout <= 0L)
                    return false;
                if (shouldParkAfterFailedAcquire(p, node) &&
                    nanosTimeout > spinForTimeoutThreshold)
                    LockSupport.parkNanos(this, nanosTimeout);
                if (Thread.interrupted())
                    throw new InterruptedException();
            }
        } finally {
            if (failed)
                cancelAcquire(node);
        }
    }

    // Main exported methods

    /**
     * Attempts to acquire in exclusive mode. This method should query
     * if the state of the object permits it to be acquired in the
     * exclusive mode, and if so to acquire it.
     *
     * <p>This method is always invoked by the thread performing
     * acquire.  If this method reports failure, the acquire method
     * may queue the thread, if it is not already queued, until it is
     * signalled by a release from some other thread. This can be used
     * to implement method {@link Lock#tryLock()}.
     *
     * <p>The default
     * implementation throws {@link UnsupportedOperationException}.
     *
     * @param arg the acquire argument. This value is always the one
     *        passed to an acquire method, or is the value saved on entry
     *        to a condition wait.  The value is otherwise uninterpreted
     *        and can represent anything you like.
     * @return {@code true} if successful. Upon success, this object has
     *         been acquired.
     * @throws IllegalMonitorStateException if acquiring would place this
     *         synchronizer in an illegal state. This exception must be
     *         thrown in a consistent fashion for synchronization to work
     *         correctly.
     * @throws UnsupportedOperationException if exclusive mode is not supported
     */
    protected boolean tryAcquire(int arg) {
        throw new UnsupportedOperationException();
    }

    /**
     * Attempts to set the state to reflect a release in exclusive
     * mode.
     *
     * <p>This method is always invoked by the thread performing release.
     *
     * <p>The default implementation throws
     * {@link UnsupportedOperationException}.
     *
     * @param arg the release argument. This value is always the one
     *        passed to a release method, or the current state value upon
     *        entry to a condition wait.  The value is otherwise
     *        uninterpreted and can represent anything you like.
     * @return {@code true} if this object is now in a fully released
     *         state, so that any waiting threads may attempt to acquire;
     *         and {@code false} otherwise.
     * @throws IllegalMonitorStateException if releasing would place this
     *         synchronizer in an illegal state. This exception must be
     *         thrown in a consistent fashion for synchronization to work
     *         correctly.
     * @throws UnsupportedOperationException if exclusive mode is not supported
     */
    protected boolean tryRelease(int arg) {
        throw new UnsupportedOperationException();
    }

    /**
     * Attempts to acquire in shared mode. This method should query if
     * the state of the object permits it to be acquired in the shared
     * mode, and if so to acquire it.
     *
     * <p>This method is always invoked by the thread performing
     * acquire.  If this method reports failure, the acquire method
     * may queue the thread, if it is not already queued, until it is
     * signalled by a release from some other thread.
     *
     * <p>The default implementation throws {@link
     * UnsupportedOperationException}.
     *
     * @param arg the acquire argument. This value is always the one
     *        passed to an acquire method, or is the value saved on entry
     *        to a condition wait.  The value is otherwise uninterpreted
     *        and can represent anything you like.
     * @return a negative value on failure; zero if acquisition in shared
     *         mode succeeded but no subsequent shared-mode acquire can
     *         succeed; and a positive value if acquisition in shared
     *         mode succeeded and subsequent shared-mode acquires might
     *         also succeed, in which case a subsequent waiting thread
     *         must check availability. (Support for three different
     *         return values enables this method to be used in contexts
     *         where acquires only sometimes act exclusively.)  Upon
     *         success, this object has been acquired.
     * @throws IllegalMonitorStateException if acquiring would place this
     *         synchronizer in an illegal state. This exception must be
     *         thrown in a consistent fashion for synchronization to work
     *         correctly.
     * @throws UnsupportedOperationException if shared mode is not supported
     */
    protected int tryAcquireShared(int arg) {
        throw new UnsupportedOperationException();
    }

    /**
     * Attempts to set the state to reflect a release in shared mode.
     *
     * <p>This method is always invoked by the thread performing release.
     *
     * <p>The default implementation throws
     * {@link UnsupportedOperationException}.
     *
     * @param arg the release argument. This value is always the one
     *        passed to a release method, or the current state value upon
     *        entry to a condition wait.  The value is otherwise
     *        uninterpreted and can represent anything you like.
     * @return {@code true} if this release of shared mode may permit a
     *         waiting acquire (shared or exclusive) to succeed; and
     *         {@code false} otherwise
     * @throws IllegalMonitorStateException if releasing would place this
     *         synchronizer in an illegal state. This exception must be
     *         thrown in a consistent fashion for synchronization to work
     *         correctly.
     * @throws UnsupportedOperationException if shared mode is not supported
     */
    protected boolean tryReleaseShared(int arg) {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns {@code true} if synchronization is held exclusively with
     * respect to the current (calling) thread.  This method is invoked
     * upon each call to a non-waiting {@link ConditionObject} method.
     * (Waiting methods instead invoke {@link #release}.)
     *
     * <p>The default implementation throws {@link
     * UnsupportedOperationException}. This method is invoked
     * internally only within {@link ConditionObject} methods, so need
     * not be defined if conditions are not used.
     *
     * @return {@code true} if synchronization is held exclusively;
     *         {@code false} otherwise
     * @throws UnsupportedOperationException if conditions are not supported
     */
    protected boolean isHeldExclusively() {
        throw new UnsupportedOperationException();
    }

    /**
     * �Զ�ռ��ʽռ����
     *
     * @param arg
     */
    public final void acquire(int arg) {
    	// tryAcquire : ��ͼ�ڶ�ռģʽ�»�ȡ����
        if (!tryAcquire(arg) &&
            acquireQueued(addWaiter(Node.EXCLUSIVE), arg))
        	// ����ڻ�ȡ���Ĺ������жϹ�,�������жϱ�ʶ
            selfInterrupt();
    }

    /**
     * Acquires in exclusive mode, aborting if interrupted.
     * Implemented by first checking interrupt status, then invoking
     * at least once {@link #tryAcquire}, returning on
     * success.  Otherwise the thread is queued, possibly repeatedly
     * blocking and unblocking, invoking {@link #tryAcquire}
     * until success or the thread is interrupted.  This method can be
     * used to implement method {@link Lock#lockInterruptibly}.
     *
     * @param arg the acquire argument.  This value is conveyed to
     *        {@link #tryAcquire} but is otherwise uninterpreted and
     *        can represent anything you like.
     * @throws InterruptedException if the current thread is interrupted
     */
    public final void acquireInterruptibly(int arg)
            throws InterruptedException {
    	// �����ж����߳��Ƿ��жϹ�,����жϹ��׳�InterruptedException,�������߳��ж�״̬
    	// ��ʱ�̲߳�δ����ͬ������
        if (Thread.interrupted())
            throw new InterruptedException();
        // ����tryAcquire���Ի�ȡ��,��δ��ȡ����,ִ��doAcquireInterruptibly
        if (!tryAcquire(arg))
            doAcquireInterruptibly(arg);
    }

    /**
     * Attempts to acquire in exclusive mode, aborting if interrupted,
     * and failing if the given timeout elapses.  Implemented by first
     * checking interrupt status, then invoking at least once {@link
     * #tryAcquire}, returning on success.  Otherwise, the thread is
     * queued, possibly repeatedly blocking and unblocking, invoking
     * {@link #tryAcquire} until success or the thread is interrupted
     * or the timeout elapses.  This method can be used to implement
     * method {@link Lock#tryLock(long, TimeUnit)}.
     *
     * @param arg the acquire argument.  This value is conveyed to
     *        {@link #tryAcquire} but is otherwise uninterpreted and
     *        can represent anything you like.
     * @param nanosTimeout the maximum number of nanoseconds to wait
     * @return {@code true} if acquired; {@code false} if timed out
     * @throws InterruptedException if the current thread is interrupted
     */
    public final boolean tryAcquireNanos(int arg, long nanosTimeout)
            throws InterruptedException {
        if (Thread.interrupted())
            throw new InterruptedException();
        return tryAcquire(arg) ||
            doAcquireNanos(arg, nanosTimeout);
    }

    /**
     * Releases in exclusive mode.  Implemented by unblocking one or
     * more threads if {@link #tryRelease} returns true.
     * This method can be used to implement method {@link Lock#unlock}.
     *
     * @param arg the release argument.  This value is conveyed to
     *        {@link #tryRelease} but is otherwise uninterpreted and
     *        can represent anything you like.
     * @return the value returned from {@link #tryRelease}
     */
    public final boolean release(int arg) {
        if (tryRelease(arg)) {
            Node h = head;
            // ���ͷ��㲻�Ƕ����е����һ���ڵ㣬ִ��unparkSuccessor
            if (h != null && h.waitStatus != 0)
                unparkSuccessor(h);
            return true;
        }
        return false;
    }

    /**
     * Acquires in shared mode, ignoring interrupts.  Implemented by
     * first invoking at least once {@link #tryAcquireShared},
     * returning on success.  Otherwise the thread is queued, possibly
     * repeatedly blocking and unblocking, invoking {@link
     * #tryAcquireShared} until success.
     *
     * @param arg the acquire argument.  This value is conveyed to
     *        {@link #tryAcquireShared} but is otherwise uninterpreted
     *        and can represent anything you like.
     */
    public final void acquireShared(int arg) {
        if (tryAcquireShared(arg) < 0)
            doAcquireShared(arg);
    }

    /**
     * �����������ж�ʱ�׳�InterruptedException
     * @param arg
     * @throws InterruptedException
     */
    public final void acquireSharedInterruptibly(int arg)
            throws InterruptedException {
        if (Thread.interrupted())
            throw new InterruptedException();
        // (getState() == 0) ? 1 : -1
        // ���tryAcquireShared >= 0 ֱ�ӷ���
        // tryAcquireShared < 0 �� CountDownLatch#count����0
        if (tryAcquireShared(arg) < 0)
        	// state==0ʱ��ֱ�ӷ���
        	// state!=0ʱ����ʵ����state>0������ִ��
        	// �����������ж�ʱ�׳�InterruptedException
            doAcquireSharedInterruptibly(arg);
    }

    /**
     * Attempts to acquire in shared mode, aborting if interrupted, and
     * failing if the given timeout elapses.  Implemented by first
     * checking interrupt status, then invoking at least once {@link
     * #tryAcquireShared}, returning on success.  Otherwise, the
     * thread is queued, possibly repeatedly blocking and unblocking,
     * invoking {@link #tryAcquireShared} until success or the thread
     * is interrupted or the timeout elapses.
     *
     * @param arg the acquire argument.  This value is conveyed to
     *        {@link #tryAcquireShared} but is otherwise uninterpreted
     *        and can represent anything you like.
     * @param nanosTimeout the maximum number of nanoseconds to wait
     * @return {@code true} if acquired; {@code false} if timed out
     * @throws InterruptedException if the current thread is interrupted
     */
    public final boolean tryAcquireSharedNanos(int arg, long nanosTimeout)
            throws InterruptedException {
        if (Thread.interrupted())
            throw new InterruptedException();
        return tryAcquireShared(arg) >= 0 ||
            doAcquireSharedNanos(arg, nanosTimeout);
    }

    /**
     * Releases in shared mode.  Implemented by unblocking one or more
     * threads if {@link #tryReleaseShared} returns true.
     *
     * @param arg the release argument.  This value is conveyed to
     *        {@link #tryReleaseShared} but is otherwise uninterpreted
     *        and can represent anything you like.
     * @return the value returned from {@link #tryReleaseShared}
     */
    public final boolean releaseShared(int arg) {
        if (tryReleaseShared(arg)) {
            doReleaseShared();
            return true;
        }
        return false;
    }

    // Queue inspection methods

    /**
     * Queries whether any threads are waiting to acquire. Note that
     * because cancellations due to interrupts and timeouts may occur
     * at any time, a {@code true} return does not guarantee that any
     * other thread will ever acquire.
     *
     * <p>In this implementation, this operation returns in
     * constant time.
     *
     * @return {@code true} if there may be other threads waiting to acquire
     */
    public final boolean hasQueuedThreads() {
        return head != tail;
    }

    /**
     * Queries whether any threads have ever contended to acquire this
     * synchronizer; that is if an acquire method has ever blocked.
     *
     * <p>In this implementation, this operation returns in
     * constant time.
     *
     * @return {@code true} if there has ever been contention
     */
    public final boolean hasContended() {
        return head != null;
    }

    /**
     * Returns the first (longest-waiting) thread in the queue, or
     * {@code null} if no threads are currently queued.
     *
     * <p>In this implementation, this operation normally returns in
     * constant time, but may iterate upon contention if other threads are
     * concurrently modifying the queue.
     *
     * @return the first (longest-waiting) thread in the queue, or
     *         {@code null} if no threads are currently queued
     */
    public final Thread getFirstQueuedThread() {
        // handle only fast path, else relay
        return (head == tail) ? null : fullGetFirstQueuedThread();
    }

    /**
     * Version of getFirstQueuedThread called when fastpath fails
     */
    private Thread fullGetFirstQueuedThread() {
        /*
         * The first node is normally head.next. Try to get its
         * thread field, ensuring consistent reads: If thread
         * field is nulled out or s.prev is no longer head, then
         * some other thread(s) concurrently performed setHead in
         * between some of our reads. We try this twice before
         * resorting to traversal.
         */
        Node h, s;
        Thread st;
        if (((h = head) != null && (s = h.next) != null &&
             s.prev == head && (st = s.thread) != null) ||
            ((h = head) != null && (s = h.next) != null &&
             s.prev == head && (st = s.thread) != null))
            return st;

        /*
         * Head's next field might not have been set yet, or may have
         * been unset after setHead. So we must check to see if tail
         * is actually first node. If not, we continue on, safely
         * traversing from tail back to head to find first,
         * guaranteeing termination.
         */

        Node t = tail;
        Thread firstThread = null;
        while (t != null && t != head) {
            Thread tt = t.thread;
            if (tt != null)
                firstThread = tt;
            t = t.prev;
        }
        return firstThread;
    }

    /**
     * Returns true if the given thread is currently queued.
     *
     * <p>This implementation traverses the queue to determine
     * presence of the given thread.
     *
     * @param thread the thread
     * @return {@code true} if the given thread is on the queue
     * @throws NullPointerException if the thread is null
     */
    public final boolean isQueued(Thread thread) {
        if (thread == null)
            throw new NullPointerException();
        for (Node p = tail; p != null; p = p.prev)
            if (p.thread == thread)
                return true;
        return false;
    }

    /**
     * Returns {@code true} if the apparent first queued thread, if one
     * exists, is waiting in exclusive mode.  If this method returns
     * {@code true}, and the current thread is attempting to acquire in
     * shared mode (that is, this method is invoked from {@link
     * #tryAcquireShared}) then it is guaranteed that the current thread
     * is not the first queued thread.  Used only as a heuristic in
     * ReentrantReadWriteLock.
     */
    final boolean apparentlyFirstQueuedIsExclusive() {
        Node h, s;
        return (h = head) != null &&
            (s = h.next)  != null &&
            !s.isShared()         &&
            s.thread != null;
    }

    /**
     * �ж��Ƿ���ڱȵ�ǰ�̵߳ȴ�ʱ��������̣߳����������̡߳���ӡ�
     *
     */
    public final boolean hasQueuedPredecessors() {
        // tail ������е����һ���߳�
    	// head ������еĵ�һ���߳�
    	// 1. tail == head ʱ,����false
    	// 2. tail != head �� ͷ�ڵ�ĺ�̽ڵ�Ϊnull,����true
    	// 3. tail != head �� ͷ�ڵ�ĺ�̽ڵ㲻Ϊ��ǰ�̣߳�����true
    	Node t = tail; 
        Node h = head;
        Node s;
        return h != t &&
            ((s = h.next) == null || s.thread != Thread.currentThread());
    }


    // Instrumentation and monitoring methods

    /**
     * Returns an estimate of the number of threads waiting to
     * acquire.  The value is only an estimate because the number of
     * threads may change dynamically while this method traverses
     * internal data structures.  This method is designed for use in
     * monitoring system state, not for synchronization
     * control.
     *
     * @return the estimated number of threads waiting to acquire
     */
    public final int getQueueLength() {
        int n = 0;
        for (Node p = tail; p != null; p = p.prev) {
            if (p.thread != null)
                ++n;
        }
        return n;
    }

    /**
     * Returns a collection containing threads that may be waiting to
     * acquire.  Because the actual set of threads may change
     * dynamically while constructing this result, the returned
     * collection is only a best-effort estimate.  The elements of the
     * returned collection are in no particular order.  This method is
     * designed to facilitate construction of subclasses that provide
     * more extensive monitoring facilities.
     *
     * @return the collection of threads
     */
    public final Collection<Thread> getQueuedThreads() {
        ArrayList<Thread> list = new ArrayList<Thread>();
        for (Node p = tail; p != null; p = p.prev) {
            Thread t = p.thread;
            if (t != null)
                list.add(t);
        }
        return list;
    }

    /**
     * Returns a collection containing threads that may be waiting to
     * acquire in exclusive mode. This has the same properties
     * as {@link #getQueuedThreads} except that it only returns
     * those threads waiting due to an exclusive acquire.
     *
     * @return the collection of threads
     */
    public final Collection<Thread> getExclusiveQueuedThreads() {
        ArrayList<Thread> list = new ArrayList<Thread>();
        for (Node p = tail; p != null; p = p.prev) {
            if (!p.isShared()) {
                Thread t = p.thread;
                if (t != null)
                    list.add(t);
            }
        }
        return list;
    }

    /**
     * Returns a collection containing threads that may be waiting to
     * acquire in shared mode. This has the same properties
     * as {@link #getQueuedThreads} except that it only returns
     * those threads waiting due to a shared acquire.
     *
     * @return the collection of threads
     */
    public final Collection<Thread> getSharedQueuedThreads() {
        ArrayList<Thread> list = new ArrayList<Thread>();
        for (Node p = tail; p != null; p = p.prev) {
            if (p.isShared()) {
                Thread t = p.thread;
                if (t != null)
                    list.add(t);
            }
        }
        return list;
    }

    /**
     * Returns a string identifying this synchronizer, as well as its state.
     * The state, in brackets, includes the String {@code "State ="}
     * followed by the current value of {@link #getState}, and either
     * {@code "nonempty"} or {@code "empty"} depending on whether the
     * queue is empty.
     *
     * @return a string identifying this synchronizer, as well as its state
     */
    public String toString() {
        int s = getState();
        String q  = hasQueuedThreads() ? "non" : "";
        return super.toString() +
            "[State = " + s + ", " + q + "empty queue]";
    }


    // Internal support methods for Conditions

    /**
     * �жϽڵ��Ƿ��Ѿ�����������ת�Ƶ�ͬ������
     * @param node the node
     * @return true if is reacquiring
     */
    final boolean isOnSyncQueue(Node node) {
    	// 1.�ڵ������������ʱ���ȴ�״̬ΪCONDITION���ڽڵ�ת�ƹ����У��Ὣ�ȴ�״̬����Ϊ0
    	// ��������ڵ�ĵȴ�״̬ΪCONDITION��˵���ڵ�һ���������������У�
    	// 2.ת�ƹ����л��������ýڵ��ͬ������ǰ���ڵ�����prev��
    	// ����ڵ��ͬ������ǰ���ڵ�����Ϊnull��˵���ڵ�һ���������������У�
        if (node.waitStatus == Node.CONDITION || node.prev == null)
            return false;
        if (node.next != null)
        	// ����ڵ�ӵ����ͬ�����еĺ�̽ڵ�next����ô�ڵ�һ���Ѿ�ת�Ƶ���ͬ��������
            return true;
        // ��ͬ�����е�β�ڵ���ǰ���������ܷ��ҵ��ڵ�node
        // ͬ��������������β��ӽڵ�,��˴Ӷ�β��ʼ����Ч�ʸ���
        return findNodeFromTail(node);
    }

    /**
     * ���node�ڵ��ڶ������򷵻�true
     * @return true if present
     */
    private boolean findNodeFromTail(Node node) {
        Node t = tail;
        for (;;) {
            if (t == node)
                return true;
            if (t == null)
                return false;
            t = t.prev;
        }
    }

    /**
     * Transfers a node from a condition queue onto sync queue.
     * Returns true if successful.
     * @param node the node
     * @return true if successfully transferred (else the node was
     * cancelled before signal)
     */
    final boolean transferForSignal(Node node) {
    	// ת�ƽڵ�֮ǰ���Ƚ���ȴ�״̬����Ϊ0
    	// �ı�ȴ������еĵ�һ���ڵ��״̬Ϊ0,�ɹ�����ִ��,���ɹ�ֱ�ӷ���false
    	// ����ReentrantLock.lock()������ʧ��ʱ����װ�ɽڵ㲢׼������ͬ�����еĳ�������һ��
    	// ��ʱ�ڵ�ĵȴ�״̬Ҳ��0����˵�ǰ�ڵ�׼������ͬ������ǰ���ȴ�״̬Ҳ����Ϊ0
        if (!compareAndSetWaitStatus(node, Node.CONDITION, 0))
        	// �ڵ�ĵȴ�״̬Ϊ0����transferForSignal������ǰ���߳����ж϶��˳�����״̬������ִ��await()���δ���
        	// ���ͨ��transferAfterCancelledWait��У���жϷ�����transferForSignal
        	// ֮ǰ����transferForSignal֮��
        	// �����֮ǰ����ô��ʱ��Ԥ��ֵΪ0��CAS��ʧ�ܣ�ֱ�ӷ���false��
            return false;
        // ����enq�������ýڵ���ӵ�ͬ�����еĶ�β
        // p����ԭ��ͬ�����еĶ�β
        Node p = enq(node);
        int ws = p.waitStatus;
        // 1. ws>0����ǰ���ڵ�Ϊȡ��״̬,ִ�л��ѵ�ǰ�߳�,�̱߳����Ѻ�����ִ��await()�ĺ��δ���
        // 2. ���ws<=0����ͳһ��ǰ���ڵ����ΪSIGNAL����ʾ��ǰ���ڵ�ȡ��ʱ���ܹ����ѵ�ǰ�ڵ㣬��ǰ�ڵ���Ա���ȫ�ع���
        // ���CAS����ʧ�ܣ���ֱ�ӻ��ѵ�ǰ�ڵ�
        if (ws > 0 || !compareAndSetWaitStatus(p, ws, Node.SIGNAL))
            LockSupport.unpark(node.thread);
        return true;
    }

    final boolean transferAfterCancelledWait(Node node) {
    	// ����߳��жϷ�����ConditionObject.signal()����֮ǰ��ִ����Ӳ�����
    	// ����true����ӦTHROW_IE
    	// ����߳��жϷ�����ConditionObject.signal()����֮�������ȴ���Ӳ�����ɣ�
    	// ����false����ӦREINTERRUPT
    	// ���㷢���жϣ�Ҳ��������ɽڵ��ת�ƣ���һ�����Ҫ����
        if (compareAndSetWaitStatus(node, Node.CONDITION, 0)) {
        	// ������³ɹ�,˵��transferForSignal����û��ִ��
        	// ˵���жϷ�����signal����֮ǰ
        	// ����transferForSignal��ִ�еĲ������ڵ���뵽ͬ��������
            enq(node);
            return true;
        }
        // ִ�е�����˵��node����״̬ʧ��,��node�ڵ��״̬�Ѿ�Ϊ0,�жϷ�����signal֮��
        while (!isOnSyncQueue(node))
            Thread.yield();
        return false;
    }

    /**
     * Invokes release with current state value; returns saved state.
     * Cancels node and throws exception on failure.
     * @param node the condition node for this wait
     * @return previous sync state
     */
    final int fullyRelease(Node node) {
        boolean failed = true;
        try {
        	// �˴���ȡ����ReentrantLock�������״̬,��ν���ͷ���Ҳ���ͷ���ReentrantLock������
            int savedState = getState();
            // �����ͷŵ�ǰ�̳߳��е�����ReentrantLock��,����˴��õ���savedState>1,����׳�
            // IllegalMonitorStateException�쳣
            if (release(savedState)) {
                failed = false;
                return savedState;
            } else {
                throw new IllegalMonitorStateException();
            }
        } finally {
        	// �ͷ���ʧ�ܣ����õ�ǰ�ڵ�ĵȴ�״̬ΪCANCELLED
            if (failed)
                node.waitStatus = Node.CANCELLED;
        }
    }

    // Instrumentation methods for conditions

    /**
     * Queries whether the given ConditionObject
     * uses this synchronizer as its lock.
     *
     * @param condition the condition
     * @return {@code true} if owned
     * @throws NullPointerException if the condition is null
     */
    public final boolean owns(ConditionObject condition) {
        return condition.isOwnedBy(this);
    }

    /**
     * Queries whether any threads are waiting on the given condition
     * associated with this synchronizer. Note that because timeouts
     * and interrupts may occur at any time, a {@code true} return
     * does not guarantee that a future {@code signal} will awaken
     * any threads.  This method is designed primarily for use in
     * monitoring of the system state.
     *
     * @param condition the condition
     * @return {@code true} if there are any waiting threads
     * @throws IllegalMonitorStateException if exclusive synchronization
     *         is not held
     * @throws IllegalArgumentException if the given condition is
     *         not associated with this synchronizer
     * @throws NullPointerException if the condition is null
     */
    public final boolean hasWaiters(ConditionObject condition) {
        if (!owns(condition))
            throw new IllegalArgumentException("Not owner");
        return condition.hasWaiters();
    }

    /**
     * Returns an estimate of the number of threads waiting on the
     * given condition associated with this synchronizer. Note that
     * because timeouts and interrupts may occur at any time, the
     * estimate serves only as an upper bound on the actual number of
     * waiters.  This method is designed for use in monitoring of the
     * system state, not for synchronization control.
     *
     * @param condition the condition
     * @return the estimated number of waiting threads
     * @throws IllegalMonitorStateException if exclusive synchronization
     *         is not held
     * @throws IllegalArgumentException if the given condition is
     *         not associated with this synchronizer
     * @throws NullPointerException if the condition is null
     */
    public final int getWaitQueueLength(ConditionObject condition) {
        if (!owns(condition))
            throw new IllegalArgumentException("Not owner");
        return condition.getWaitQueueLength();
    }

    /**
     * Returns a collection containing those threads that may be
     * waiting on the given condition associated with this
     * synchronizer.  Because the actual set of threads may change
     * dynamically while constructing this result, the returned
     * collection is only a best-effort estimate. The elements of the
     * returned collection are in no particular order.
     *
     * @param condition the condition
     * @return the collection of threads
     * @throws IllegalMonitorStateException if exclusive synchronization
     *         is not held
     * @throws IllegalArgumentException if the given condition is
     *         not associated with this synchronizer
     * @throws NullPointerException if the condition is null
     */
    public final Collection<Thread> getWaitingThreads(ConditionObject condition) {
        if (!owns(condition))
            throw new IllegalArgumentException("Not owner");
        return condition.getWaitingThreads();
    }

    public class ConditionObject implements Condition, java.io.Serializable {
    	
        private static final long serialVersionUID = 1173984872572414699L;
        /**
         * �����ȴ����е�һ���ڵ�
         */
        private transient Node firstWaiter;
        
        /**
         * �����ȴ��������һ���ڵ�
         */
        private transient Node lastWaiter;

        public ConditionObject() { }

        /**
         * ���һ���µĽڵ㵽�ȴ�����
         * @return its new wait node
         */
        private Node addConditionWaiter() {
        	// �� �ȴ�����Ϊ��,���õ�ǰ�ڵ�Ϊ�ȴ������е�һ���ȴ��ڵ�����һ���ȴ��ڵ�
        	// �� �ȴ����в�Ϊ�գ����õ�ǰ�ڵ�Ϊ�ȴ����������һ���ȴ��ߵ���һ���ȴ��ڵ�
            Node t = lastWaiter;
            if (t != null && t.waitStatus != Node.CONDITION) {
            	// �Ͽ��̱߳�ȡ���ĵȴ��ڵ�
                unlinkCancelledWaiters();
                t = lastWaiter;
            }
            // ����һ��CONDITION�ȴ��ڵ�
            // ���ԭ����Ϊ��,����ǰ�ڵ�Ϊ�������еĵ�һ���ڵ�����һ���ڵ�
            // ���ԭ���в�Ϊ��,����ǰ�ڵ���ӵ�ԭβ�ڵ��,���õ�ǰ�ڵ�Ϊ�µ�β�ڵ�
            Node node = new Node(Thread.currentThread(), Node.CONDITION);
            if (t == null)
                firstWaiter = node;
            else
                t.nextWaiter = node;
            lastWaiter = node;
            return node;
        }

        private void doSignal(Node first) {
            do {
                if ( (firstWaiter = first.nextWaiter) == null)
                	// ����������е�ͷ���Ϊnull���������е�β�ڵ��Ϊnull
                    lastWaiter = null;
                // first��Ҫ��ת�Ƶ�ͬ�����У���Ҫ�����������жϿ�
                first.nextWaiter = null;
            } while (!transferForSignal(first) &&
                     (first = firstWaiter) != null);
            // û�гɹ�ת����Ч�ڵ㲢��δ�ﵽ��������β�ڵ㣬����ѭ��
        }

        private void doSignalAll(Node first) {
            lastWaiter = firstWaiter = null;
            do {
                Node next = first.nextWaiter;
                first.nextWaiter = null;
                transferForSignal(first);
                first = next;
            } while (first != null);
        }

        /**
         * ��ͷ��㿪ʼ�����������У����Ƴ���CONDITION�ڵ�
         */
        private void unlinkCancelledWaiters() {
            Node t = firstWaiter;
            Node trail = null;
            // �߼����ܣ�
            // �����жϵ�һ���ڵ�,���ΪCONDITION,����ýڵ�Ϊtrail,����tΪnext,��������
            // �жϵڶ����ڵ�,���ΪCONDITION,ͬһ����,����ѭ��
            // �ڶ����ڵ�����ΪCONDITION,�Ͽ��ڶ����ڵ�,����trail����һ���ڵ�Ϊt��next,
            // �ڶ����ڵ�ͱ��Ͽ���
            // ѭ���������ο��ڶ���
            // ѭ�������һ���ڵ�,��nextΪnull,Ȼ������trailΪlastWaiter�����һ��Condition�Ľڵ㣩
            while (t != null) {
                Node next = t.nextWaiter;
                if (t.waitStatus != Node.CONDITION) {
                	// �����ǰ�ڵ�״̬��ΪCONDITION,�Ͽ���ǰ�ڵ�
                    t.nextWaiter = null;
                    if (trail == null)
                        firstWaiter = next;
                    else
                        trail.nextWaiter = next;
                    if (next == null)
                    	// ��ʱ˵���Ѿ�����������β,����trailΪ����β
                        lastWaiter = trail;
                }
                else
                    trail = t;
                t = next;
            }
        }

        public final void signal() {
        	// ��ǰ�߳��Ƿ���ж�ռ��
            if (!isHeldExclusively())
                throw new IllegalMonitorStateException();
            Node first = firstWaiter;
            // �ȴ�����firstWaiter���������doSignal�������ѵ�һ���ȴ���
            if (first != null)
                doSignal(first);
        }

        public final void signalAll() {
            if (!isHeldExclusively())
                throw new IllegalMonitorStateException();
            Node first = firstWaiter;
            if (first != null)
                doSignalAll(first);
        }

        public final void awaitUninterruptibly() {
            Node node = addConditionWaiter();
            int savedState = fullyRelease(node);
            boolean interrupted = false;
            while (!isOnSyncQueue(node)) {
                LockSupport.park(this);
                if (Thread.interrupted())
                    interrupted = true;
            }
            if (acquireQueued(node, savedState) || interrupted)
                selfInterrupt();
        }

        /** �ж�ģʽ����Ҫ���������̵߳��ж�״̬ */
        private static final int REINTERRUPT =  1;
        /** �ж�ģʽ����Ҫ�׳�InterruptedException�쳣 */
        private static final int THROW_IE    = -1;

        private int checkInterruptWhileWaiting(Node node) {
        	// �����ǰ�߳����ж�״̬,Ȼ�����transferAfterCancelledWait����,���򷵻�0
        	// ����transferAfterCancelledWait����ֵ�ж��ж�ģʽ
            return Thread.interrupted() ?
                (transferAfterCancelledWait(node) ? THROW_IE : REINTERRUPT) :
                0;
        }

        private void reportInterruptAfterWait(int interruptMode)
            throws InterruptedException {
            if (interruptMode == THROW_IE)
                throw new InterruptedException();
            else if (interruptMode == REINTERRUPT)
                selfInterrupt();
        }

        public final void await() throws InterruptedException {
            // 1.�����ǰ�߳��ж�,���׳��쳣
        	if (Thread.interrupted())
                throw new InterruptedException();
        	// 2.���ڵ���뵽Condition������ȥ
            Node node = addConditionWaiter();
            // 3.�ͷŵ�ǰ�̳߳��е���
            int savedState = fullyRelease(node);
            int interruptMode = 0;
            // 4.����ת�ƽڵ㣨�������� -> ͬ�����У�������¼�ж�ģʽ
            while (!isOnSyncQueue(node)) { // isOnSyncQueue���жϽڵ��Ƿ��Ѿ�����������ת�Ƶ�ͬ������
                // ����ڵ������������У�������ǰ�߳�
            	LockSupport.park(this);
            	// ִ�е�����˵�������������
            	// �����̵߳��ж�ģʽ
                if ((interruptMode = checkInterruptWhileWaiting(node)) != 0)
                    break;
            }
            // 5.��ǰ�߳̽������,��node�ڵ���õ�ͬ�������еȴ���ȡ��
            if (acquireQueued(node, savedState) && interruptMode != THROW_IE)
                interruptMode = REINTERRUPT;
            
            // 6.������������
            // ִ�е�����˵���ڵ��Ѿ�ת�Ƶ�ͬ�������У����Ѿ���ö�ռ��������acquireQueued�Ĺ����б��жϣ�
            // ��ʱ�ڵ㲻Ӧ�ø����������й����ˣ����Ҵ�ʱ�ڵ��״̬�϶���ΪCONDITION
            // ���ִ��unlinkCancelledWaiters�������������Ƴ��ýڵ�
            if (node.nextWaiter != null)
                unlinkCancelledWaiters();
            if (interruptMode != 0)
            	// ���ݲ�ͬ���ж�ģʽ������÷����浱ǰ�̵߳��ж����
                // 1. ConditionObject.signal��������֮ǰ�ж��˵�ǰ�̣߳������׳�InterruptedException�쳣���ж��̵߳ĺ�������
                // 2. ConditionObject.signal��������֮���ж��˵�ǰ�̣߳����õ�ǰ�̵߳��ж�״̬�����̲߳�����ʵ���Ե�Ӱ��
                reportInterruptAfterWait(interruptMode);
        }

        public final long awaitNanos(long nanosTimeout)
                throws InterruptedException {
            if (Thread.interrupted())
                throw new InterruptedException();
            Node node = addConditionWaiter();
            int savedState = fullyRelease(node);
            final long deadline = System.nanoTime() + nanosTimeout;
            int interruptMode = 0;
            while (!isOnSyncQueue(node)) {
                if (nanosTimeout <= 0L) {
                    transferAfterCancelledWait(node);
                    break;
                }
                if (nanosTimeout >= spinForTimeoutThreshold)
                    LockSupport.parkNanos(this, nanosTimeout);
                if ((interruptMode = checkInterruptWhileWaiting(node)) != 0)
                    break;
                nanosTimeout = deadline - System.nanoTime();
            }
            if (acquireQueued(node, savedState) && interruptMode != THROW_IE)
                interruptMode = REINTERRUPT;
            if (node.nextWaiter != null)
                unlinkCancelledWaiters();
            if (interruptMode != 0)
                reportInterruptAfterWait(interruptMode);
            return deadline - System.nanoTime();
        }

        public final boolean awaitUntil(Date deadline)
                throws InterruptedException {
            long abstime = deadline.getTime();
            if (Thread.interrupted())
                throw new InterruptedException();
            Node node = addConditionWaiter();
            int savedState = fullyRelease(node);
            boolean timedout = false;
            int interruptMode = 0;
            while (!isOnSyncQueue(node)) {
                if (System.currentTimeMillis() > abstime) {
                    timedout = transferAfterCancelledWait(node);
                    break;
                }
                LockSupport.parkUntil(this, abstime);
                if ((interruptMode = checkInterruptWhileWaiting(node)) != 0)
                    break;
            }
            if (acquireQueued(node, savedState) && interruptMode != THROW_IE)
                interruptMode = REINTERRUPT;
            if (node.nextWaiter != null)
                unlinkCancelledWaiters();
            if (interruptMode != 0)
                reportInterruptAfterWait(interruptMode);
            return !timedout;
        }

        public final boolean await(long time, TimeUnit unit)
                throws InterruptedException {
            long nanosTimeout = unit.toNanos(time);
            if (Thread.interrupted())
                throw new InterruptedException();
            Node node = addConditionWaiter();
            int savedState = fullyRelease(node);
            final long deadline = System.nanoTime() + nanosTimeout;
            boolean timedout = false;
            int interruptMode = 0;
            while (!isOnSyncQueue(node)) {
                if (nanosTimeout <= 0L) {
                    timedout = transferAfterCancelledWait(node);
                    break;
                }
                if (nanosTimeout >= spinForTimeoutThreshold)
                    LockSupport.parkNanos(this, nanosTimeout);
                if ((interruptMode = checkInterruptWhileWaiting(node)) != 0)
                    break;
                nanosTimeout = deadline - System.nanoTime();
            }
            if (acquireQueued(node, savedState) && interruptMode != THROW_IE)
                interruptMode = REINTERRUPT;
            if (node.nextWaiter != null)
                unlinkCancelledWaiters();
            if (interruptMode != 0)
                reportInterruptAfterWait(interruptMode);
            return !timedout;
        }

        //  support for instrumentation

        /**
         * Returns true if this condition was created by the given
         * synchronization object.
         *
         * @return {@code true} if owned
         */
        final boolean isOwnedBy(AbstractQueuedSynchronizer sync) {
            return sync == AbstractQueuedSynchronizer.this;
        }

        /**
         * Queries whether any threads are waiting on this condition.
         * Implements {@link AbstractQueuedSynchronizer#hasWaiters(ConditionObject)}.
         *
         * @return {@code true} if there are any waiting threads
         * @throws IllegalMonitorStateException if {@link #isHeldExclusively}
         *         returns {@code false}
         */
        protected final boolean hasWaiters() {
            if (!isHeldExclusively())
                throw new IllegalMonitorStateException();
            for (Node w = firstWaiter; w != null; w = w.nextWaiter) {
                if (w.waitStatus == Node.CONDITION)
                    return true;
            }
            return false;
        }

        protected final int getWaitQueueLength() {
            if (!isHeldExclusively())
                throw new IllegalMonitorStateException();
            int n = 0;
            for (Node w = firstWaiter; w != null; w = w.nextWaiter) {
                if (w.waitStatus == Node.CONDITION)
                    ++n;
            }
            return n;
        }

        protected final Collection<Thread> getWaitingThreads() {
            if (!isHeldExclusively())
                throw new IllegalMonitorStateException();
            ArrayList<Thread> list = new ArrayList<Thread>();
            for (Node w = firstWaiter; w != null; w = w.nextWaiter) {
                if (w.waitStatus == Node.CONDITION) {
                    Thread t = w.thread;
                    if (t != null)
                        list.add(t);
                }
            }
            return list;
        }
    }

    /**
     * Setup to support compareAndSet. We need to natively implement
     * this here: For the sake of permitting future enhancements, we
     * cannot explicitly subclass AtomicInteger, which would be
     * efficient and useful otherwise. So, as the lesser of evils, we
     * natively implement using hotspot intrinsics API. And while we
     * are at it, we do the same for other CASable fields (which could
     * otherwise be done with atomic field updaters).
     */
    private static final Unsafe unsafe = Unsafe.getUnsafe();
    private static final long stateOffset;
    private static final long headOffset;
    private static final long tailOffset;
    private static final long waitStatusOffset;
    private static final long nextOffset;

    static {
        try {
            stateOffset = unsafe.objectFieldOffset
                (AbstractQueuedSynchronizer.class.getDeclaredField("state"));
            headOffset = unsafe.objectFieldOffset
                (AbstractQueuedSynchronizer.class.getDeclaredField("head"));
            tailOffset = unsafe.objectFieldOffset
                (AbstractQueuedSynchronizer.class.getDeclaredField("tail"));
            waitStatusOffset = unsafe.objectFieldOffset
                (Node.class.getDeclaredField("waitStatus"));
            nextOffset = unsafe.objectFieldOffset
                (Node.class.getDeclaredField("next"));

        } catch (Exception ex) { throw new Error(ex); }
    }

    /**
     * CAS head field. Used only by enq.
     */
    private final boolean compareAndSetHead(Node update) {
        return unsafe.compareAndSwapObject(this, headOffset, null, update);
    }

    /**
     * CAS tail field. Used only by enq.
     */
    private final boolean compareAndSetTail(Node expect, Node update) {
        return unsafe.compareAndSwapObject(this, tailOffset, expect, update);
    }

    /**
     * CAS waitStatus field of a node.
     */
    private static final boolean compareAndSetWaitStatus(Node node,
                                                         int expect,
                                                         int update) {
        return unsafe.compareAndSwapInt(node, waitStatusOffset,
                                        expect, update);
    }

    /**
     * CAS next field of a node.
     */
    private static final boolean compareAndSetNext(Node node,
                                                   Node expect,
                                                   Node update) {
        return unsafe.compareAndSwapObject(node, nextOffset, expect, update);
    }
}
