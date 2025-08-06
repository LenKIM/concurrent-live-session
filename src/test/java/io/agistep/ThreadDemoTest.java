package io.agistep;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

import static org.assertj.core.api.Assertions.*;

class ThreadDemoTest {

    @Test
    void threadTest() throws InterruptedException {
        SomethingCounter counter = new SomethingCounter();

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                counter.add(1);
            }
        });

        t.start();

        t.join();

        assertThat(counter.count).isEqualTo(1);
    }

    @Test
    void threadMultipleTest() throws InterruptedException {
        long start = System.currentTimeMillis();
        SomethingCounter counter = new SomethingCounter();

        Thread t = new Thread(() -> {
            for (int i = 0; i < 100000; i++) {
                counter.count = counter.count + 1;
            }
        });

        Thread t2 = new Thread(() -> {
            for (int i = 0; i < 100000; i++) {
                counter.count = counter.count + 1;
            }
        });

        t.start();
        t2.start();

        t.join();
        t2.join();
        long end = System.currentTimeMillis();

        System.out.println(end-start);
        assertThat(counter.count).isEqualTo(20000);

    }

    @Test
    void threadMultipleTestSolved_1() throws InterruptedException {

        long start = System.currentTimeMillis();

        SomethingCounter counter = new SomethingCounter();

        Thread t = new Thread(() -> {
            for (int i = 0; i < 100000; i++) {
                counter.add(1);
            }
        });

        Thread t2 = new Thread(() -> {
            for (int i = 0; i < 100000; i++) {
                counter.add(1);
            }
        });

        t.start();
        t2.start();

        t.join();
        t2.join();
        long end = System.currentTimeMillis();
        System.out.println(end-start);
        assertThat(counter.count).isEqualTo(200000);
    }

    @Test
    void threadMultipleTestSolved_2() throws InterruptedException {

        long start = System.currentTimeMillis();

        SomethingCounter counter = new SomethingCounter();

        Thread t = new Thread(() -> {
            for (int i = 0; i < 100000; i++) {
                counter.addWithLock(1);
            }
        });

        Thread t2 = new Thread(() -> {
            for (int i = 0; i < 100000; i++) {
                counter.addWithLock(1);
            }
        });

        t.start();
        t2.start();

        t.join();
        t2.join();
        long end = System.currentTimeMillis();
        System.out.println(end-start);
        assertThat(counter.count).isEqualTo(200000);
    }

    @Test
    void threadMultipleTestSolved_3() throws InterruptedException {

        long start = System.currentTimeMillis();

        SomethingCounter counter = new SomethingCounter();

        Thread t = new Thread(() -> {
            for (int i = 0; i < 100000; i++) {
                counter.addWithAtomic(1);
            }
        });

        Thread t2 = new Thread(() -> {
            for (int i = 0; i < 100000; i++) {
                counter.addWithAtomic(1);
            }
        });

        t.start();
        t2.start();

        t.join();
        t2.join();
        long end = System.currentTimeMillis();
        System.out.println(end-start);
        assertThat(counter.atomicInteger.get()).isEqualTo(200000);
    }

    @Test
    void concurrentPutAndAddTest() throws InterruptedException {
//        Map<String, Integer> counterMap = new ConcurrentHashMap<>();
        Map<String, Integer> counterMap = new HashMap<>();

        int threadCount = 10;
        int incrementPerThread = 10_000;
        String key = "myKey";
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            new Thread(() -> {
                for (int j = 0; j < incrementPerThread; j++) {
                    counterMap.compute(key, (k, v) -> v == null ? 1 : v + 1);
                }
                latch.countDown();
            }).start();
        }

        latch.await();

        int finalCount = counterMap.getOrDefault(key, 0);
        System.out.println("최종 카운트: " + finalCount);

        // 검증: 10 * 10_000 = 100_000
        assertThat(finalCount).isEqualTo(threadCount * incrementPerThread);
    }
}