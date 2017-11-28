/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

/*
 * This file is available under and governed by the GNU General Public
 * License version 2 only, as published by the Free Software Foundation.
 * However, the following notice accompanied the original version of this
 * file:
 *
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

package java.util.concurrent;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class CyclicBarrier {
	
	// CyclicBarrier�ǿ��ظ�ʹ�õģ�Generation��ʶһ��
    private static class Generation {
        boolean broken = false;
    }

    /** lock���ڿ��ƽ���CyclicBarrier */
    private final ReentrantLock lock = new ReentrantLock();
    
    // CyclicBarrier����Condition
    private final Condition trip = lock.newCondition();
    
    // ������߳���
    private final int parties;
    
    // ��Խ��CyclicBarrier֮ǰҪִ�еĶ���
    private final Runnable barrierCommand;
    
    // ��ǰ��
    private Generation generation = new Generation();

    // ��δ����CyclicBarrier���߳���
    private int count;

    // ���ѵ��������̣߳���������һ��
    // ��Ϊ��Ҫ����trip.signalAll()��������Ҫ�ȳ���lock
    // ����ʱ�������һ���̵߳���CyclicBarrier�����reset()
    private void nextGeneration() {
        trip.signalAll();
        count = parties;
        generation = new Generation();
    }

    // ��ǵ����Ѿ������ƣ������ѵ��������߳�
    // ��Ϊ��Ҫ����trip.signalAll()��������Ҫ�ȳ���lock
    private void breakBarrier() {
        generation.broken = true;
        count = parties;
        trip.signalAll();
    }

    private int dowait(boolean timed, long nanos)
        throws InterruptedException, BrokenBarrierException,
               TimeoutException {
        final ReentrantLock lock = this.lock;
        // ���Ȼ�ȡ��
        lock.lock();
        try {
        	// ��ȡ��ǰ��
            final Generation g = generation;

            if (g.broken)
            	// ���CyclicBarrier����broken״̬��ֱ���׳�BrokenBarrierException
            	// Ĭ��Ϊfalse
                throw new BrokenBarrierException();

            if (Thread.interrupted()) {
            	// ����ǰ�̱߳��ж�,�������generation#brokenΪtrue
            	// �Ժ���߳�ִ��await����ʱ��ִ����һ��if�߼�,�׳�BrokenBarrierException
                breakBarrier();
                // �׳��ж��쳣
                throw new InterruptedException();
            }

            // ��ǰ�߳�ִ����await˵����ǰ�߳��Ѿ������˹�������
            // count����ǰ�̵߳��﹫������֮ǰ����δ���﹫�����ϵ��߳���
            // index:��ǰ�̵߳��﹫������֮�󣬻�δ���﹫�����ϵ��߳���
            int index = --count;
            if (index == 0) {
            	// ִ�е�����˵�������̵߳��﹫������
                boolean ranAction = false;
                try {
                	// ִ�г�ʼ��CyclicBarrierʱ���������
                    final Runnable command = barrierCommand;
                    if (command != null)
                        command.run();
                    ranAction = true;
                    // ���ѵ��������̣߳���������һ��
                    nextGeneration();
                    return 0;
                } finally {
                    if (!ranAction)
                        breakBarrier();
                }
            }

            // ִ�е�����˵�������߳�û�е��﹫������
            for (;;) {
                try {
                    if (!timed)
                    	// ����trip.await������ǰ�߳�,�ȴ�������
                        trip.await();
                    else if (nanos > 0L)
                        nanos = trip.awaitNanos(nanos);
                } catch (InterruptedException ie) {
                    if (g == generation && ! g.broken) {
                        breakBarrier();
                        throw ie;
                    } else {
                        Thread.currentThread().interrupt();
                    }
                }

                if (g.broken)
                    throw new BrokenBarrierException();

                if (g != generation)
                    return index;

                if (timed && nanos <= 0L) {
                    breakBarrier();
                    throw new TimeoutException();
                }
            }
        } finally {
        	// �ͷ���
            lock.unlock();
        }
    }

    public CyclicBarrier(int parties, Runnable barrierAction) {
        if (parties <= 0) throw new IllegalArgumentException();
        // ���ò�����߳���
        this.parties = parties;
        this.count = parties;
        // �����̵߳��﹫�����Ϻ�CyclicBarrier��ִ�е�����
        this.barrierCommand = barrierAction;
    }

    public CyclicBarrier(int parties) {
        this(parties, null);
    }

    public int getParties() {
        return parties;
    }

    public int await() throws InterruptedException, BrokenBarrierException {
        try {
            return dowait(false, 0L);
        } catch (TimeoutException toe) {
            throw new Error(toe); // cannot happen
        }
    }

    public int await(long timeout, TimeUnit unit)
        throws InterruptedException,
               BrokenBarrierException,
               TimeoutException {
        return dowait(true, unit.toNanos(timeout));
    }

    // CyclicBarrier�Ƿ���broken״̬
    public boolean isBroken() {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            return generation.broken;
        } finally {
            lock.unlock();
        }
    }

    // ����CyclicBarrierΪ��ʼ��״̬����ǵ����Ѿ������� + ���ѵ��������߳� + ��������һ��
    public void reset() {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            breakBarrier();
            nextGeneration();
        } finally {
            lock.unlock();
        }
    }

    /**
     * �Ѿ�����CyclicBarrier���߳��� = ������߳��� - ��δ����CyclicBarrier���߳���
     *
     * @return the number of parties currently blocked in {@link #await}
     */
    public int getNumberWaiting() {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            return parties - count;
        } finally {
            lock.unlock();
        }
    }
}
