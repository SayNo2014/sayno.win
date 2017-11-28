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
	
	// CyclicBarrier是可重复使用的，Generation标识一代
    private static class Generation {
        boolean broken = false;
    }

    /** lock用于控制进入CyclicBarrier */
    private final ReentrantLock lock = new ReentrantLock();
    
    // CyclicBarrier基于Condition
    private final Condition trip = lock.newCondition();
    
    // 参与的线程数
    private final int parties;
    
    // 在越过CyclicBarrier之前要执行的动作
    private final Runnable barrierCommand;
    
    // 当前代
    private Generation generation = new Generation();

    // 还未到达CyclicBarrier的线程数
    private int count;

    // 唤醒当代所有线程，并开启新一代
    // 因为需要调用trip.signalAll()，所以需要先持有lock
    // 触发时机：最后一个线程到达CyclicBarrier或调用reset()
    private void nextGeneration() {
        trip.signalAll();
        count = parties;
        generation = new Generation();
    }

    // 标记当代已经被打破，并唤醒当代所有线程
    // 因为需要调用trip.signalAll()，所以需要先持有lock
    private void breakBarrier() {
        generation.broken = true;
        count = parties;
        trip.signalAll();
    }

    private int dowait(boolean timed, long nanos)
        throws InterruptedException, BrokenBarrierException,
               TimeoutException {
        final ReentrantLock lock = this.lock;
        // 首先获取锁
        lock.lock();
        try {
        	// 获取当前代
            final Generation g = generation;

            if (g.broken)
            	// 如果CyclicBarrier处于broken状态，直接抛出BrokenBarrierException
            	// 默认为false
                throw new BrokenBarrierException();

            if (Thread.interrupted()) {
            	// 若当前线程被中断,则会设置generation#broken为true
            	// 以后的线程执行await方法时会执行上一个if逻辑,抛出BrokenBarrierException
                breakBarrier();
                // 抛出中断异常
                throw new InterruptedException();
            }

            // 当前线程执行了await说明当前线程已经到达了公共屏障
            // count：当前线程到达公共屏障之前，还未到达公共屏障的线程数
            // index:当前线程到达公共屏障之后，还未到达公共屏障的线程数
            int index = --count;
            if (index == 0) {
            	// 执行到这里说明所有线程到达公共屏障
                boolean ranAction = false;
                try {
                	// 执行初始化CyclicBarrier时定义的命令
                    final Runnable command = barrierCommand;
                    if (command != null)
                        command.run();
                    ranAction = true;
                    // 唤醒当代所有线程，并开启新一代
                    nextGeneration();
                    return 0;
                } finally {
                    if (!ranAction)
                        breakBarrier();
                }
            }

            // 执行到这里说明还有线程没有到达公共屏障
            for (;;) {
                try {
                    if (!timed)
                    	// 调用trip.await阻塞当前线程,等待被唤醒
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
        	// 释放锁
            lock.unlock();
        }
    }

    public CyclicBarrier(int parties, Runnable barrierAction) {
        if (parties <= 0) throw new IllegalArgumentException();
        // 设置参与的线程数
        this.parties = parties;
        this.count = parties;
        // 所有线程到达公共屏障后，CyclicBarrier所执行的任务。
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

    // CyclicBarrier是否处于broken状态
    public boolean isBroken() {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            return generation.broken;
        } finally {
            lock.unlock();
        }
    }

    // 重置CyclicBarrier为初始化状态：标记当代已经被打破 + 唤醒当代所有线程 + 并开启新一代
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
     * 已经到达CyclicBarrier的线程数 = 参与的线程数 - 还未到达CyclicBarrier的线程数
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
