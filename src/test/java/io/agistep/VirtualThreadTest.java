package io.agistep;

import org.junit.jupiter.api.Test;

class VirtualThreadTest {

    @Test
    void virtualThreadTest() throws InterruptedException {
        Runnable task = () -> {
            System.out.println(Thread.currentThread() + " 시작");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            System.out.println(Thread.currentThread() + " 종료");
        };

        Thread vThread = Thread.ofVirtual().start(task);
        vThread.join();
    }
}
