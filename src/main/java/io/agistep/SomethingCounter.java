package io.agistep;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class SomethingCounter {

    private final Lock lock = new ReentrantLock();
    AtomicInteger atomicInteger = new AtomicInteger();

    int count = 0;

    public synchronized int add(int t) {
        count = count + t;
        return count;
    }

    public int addWithLock(int t) {
        lock.lock();
        try {
            count = count + t;
        } finally {
            lock.unlock();
        }

        return count;
    }

    public int addWithAtomic(int t) {
        return atomicInteger.addAndGet(t);
    }

    public void printCount() {
        System.out.println("모든 횟수: " + count);
    }
}
