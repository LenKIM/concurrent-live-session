package io.agistep;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.*;

class ConcurrentControlTest {


    @Test
    void executorService() throws InterruptedException {

        ExecutorService executorService = Executors.newFixedThreadPool(10);
        try {

            // Runnable VS Callable
            executorService.submit(new Callable<String>() {
                @Override
                public String call() throws Exception {
                    delay(1000);
                    System.out.println(Thread.currentThread().getName() + ": callable 1 수행 완료");
                    return "Hello";
                }
            });

            executorService.submit(new Callable<String>() {
                @Override
                public String call() throws Exception {
                    delay(1000);
                    System.out.println(Thread.currentThread().getName() + ": callable 2 수행 완료");
                    return "World";
                }
            });


        } finally {
            executorService.shutdown();
            executorService.awaitTermination(1, TimeUnit.MINUTES);
        }
    }


    @Test
    void executorService2() throws ExecutionException, InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        try {

            // Runnable VS Callable
            Future<String> submit = executorService.submit(new Callable<String>() {
                @Override
                public String call() throws Exception {
                    delay(1000);
                    System.out.println(Thread.currentThread().getName() + ": callable 1 수행 완료");
                    return "Hello";
                }
            });

            Future<String> submit1 = executorService.submit(new Callable<String>() {
                @Override
                public String call() throws Exception {
                    delay(1000);
                    System.out.println(Thread.currentThread().getName() + ": callable 2 수행 완료");
                    return "World";
                }
            });

            String s1 = submit.get();
            String s = submit1.get();
        } finally {
            executorService.shutdown();
        }
    }

    @Test
    void name() throws ExecutionException, InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(4);

        CompletableFuture<String> future1 = CompletableFuture.supplyAsync(() -> {
            delay(1000);
            System.out.println(Thread.currentThread().getName() + ": future1 수행 완료");
            return "Hello";
        }, executor);

        CompletableFuture<String> future2 = CompletableFuture.supplyAsync(() -> {
            delay(800);
            System.out.println(Thread.currentThread().getName() + ": future2 수행 완료");
            return "world";
        }, executor);


        CompletableFuture<String> combined = future1.thenCombine(future2, (f1, f2) -> f1 + " " + f2);

        System.out.println("결과: " + combined.get());

    }

    private static void delay(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ignored) {
        }
    }
}
