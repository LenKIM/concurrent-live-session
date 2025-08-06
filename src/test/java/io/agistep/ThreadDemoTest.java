package io.agistep;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

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
}