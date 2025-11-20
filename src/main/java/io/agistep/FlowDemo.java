package io.agistep;

import java.util.concurrent.Flow;
import java.util.concurrent.SubmissionPublisher;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

/**
 * Java 9 Flow API 활용 예시
 * 
 * Flow API는 Reactive Streams 표준을 구현한 것으로,
 * Publisher와 Subscriber 패턴을 통해 비동기 스트림 처리를 지원합니다.
 */
public class FlowDemo {

    /**
     * 간단한 Publisher 예시
     * SubmissionPublisher를 사용하여 데이터를 발행합니다.
     */
    public static class NumberPublisher {
        private final SubmissionPublisher<Integer> publisher;

        public NumberPublisher() {
            this.publisher = new SubmissionPublisher<>();
        }

        public Flow.Publisher<Integer> getPublisher() {
            return publisher;
        }

        public void publishNumbers(int count) {
            for (int i = 1; i <= count; i++) {
                publisher.submit(i);
            }
            publisher.close();
        }
    }

    /**
     * 간단한 Subscriber 예시
     * 발행된 데이터를 구독하여 처리합니다.
     */
    public static class NumberSubscriber implements Flow.Subscriber<Integer> {
        private Flow.Subscription subscription;
        private final AtomicInteger sum = new AtomicInteger(0);
        private final String name;

        public NumberSubscriber(String name) {
            this.name = name;
        }

        @Override
        public void onSubscribe(Flow.Subscription subscription) {
            this.subscription = subscription;
            System.out.println(name + ": 구독 시작");
            // 처음에 1개의 아이템을 요청 (배압 제어)
            subscription.request(1);
        }

        @Override
        public void onNext(Integer item) {
            System.out.println(name + ": " + item + " 수신");
            sum.addAndGet(item);
            // 다음 아이템을 요청 (배압 제어)
            subscription.request(1);
        }

        @Override
        public void onError(Throwable throwable) {
            System.err.println(name + ": 에러 발생 - " + throwable.getMessage());
        }

        @Override
        public void onComplete() {
            System.out.println(name + ": 구독 완료, 총합: " + sum.get());
        }

        public int getSum() {
            return sum.get();
        }
    }

    /**
     * 배압(Backpressure) 제어를 보여주는 Subscriber
     * 한 번에 여러 개의 아이템을 요청할 수 있습니다.
     */
    public static class BufferedSubscriber implements Flow.Subscriber<Integer> {
        private Flow.Subscription subscription;
        private final AtomicInteger count = new AtomicInteger(0);
        private final int bufferSize;

        public BufferedSubscriber(int bufferSize) {
            this.bufferSize = bufferSize;
        }

        @Override
        public void onSubscribe(Flow.Subscription subscription) {
            this.subscription = subscription;
            System.out.println("BufferedSubscriber: 구독 시작, 버퍼 크기: " + bufferSize);
            // 버퍼 크기만큼 한 번에 요청
            subscription.request(bufferSize);
        }

        @Override
        public void onNext(Integer item) {
            int current = count.incrementAndGet();
            System.out.println("BufferedSubscriber: " + item + " 수신 (총 " + current + "개)");
            
            // 버퍼가 비면 다시 요청
            if (current % bufferSize == 0) {
                subscription.request(bufferSize);
            }
        }

        @Override
        public void onError(Throwable throwable) {
            System.err.println("BufferedSubscriber: 에러 발생 - " + throwable.getMessage());
        }

        @Override
        public void onComplete() {
            System.out.println("BufferedSubscriber: 구독 완료, 총 수신: " + count.get() + "개");
        }

        public int getCount() {
            return count.get();
        }
    }

    /**
     * Processor 예시
     * Publisher와 Subscriber를 모두 구현하여 데이터 변환을 수행합니다.
     */
    public static class TransformProcessor extends SubmissionPublisher<String> 
            implements Flow.Processor<Integer, String> {
        
        private Flow.Subscription subscription;
        private final Function<Integer, String> transformer;

        public TransformProcessor(Function<Integer, String> transformer) {
            this.transformer = transformer;
        }

        @Override
        public void onSubscribe(Flow.Subscription subscription) {
            this.subscription = subscription;
            subscription.request(1);
        }

        @Override
        public void onNext(Integer item) {
            String transformed = transformer.apply(item);
            submit(transformed);
            subscription.request(1);
        }

        @Override
        public void onError(Throwable throwable) {
            closeExceptionally(throwable);
        }

        @Override
        public void onComplete() {
            close();
        }
    }

    /**
     * String을 받는 Subscriber 예시
     */
    public static class StringSubscriber implements Flow.Subscriber<String> {
        private Flow.Subscription subscription;
        private final AtomicInteger count = new AtomicInteger(0);
        private final String name;

        public StringSubscriber(String name) {
            this.name = name;
        }

        @Override
        public void onSubscribe(Flow.Subscription subscription) {
            this.subscription = subscription;
            System.out.println(name + ": 구독 시작");
            subscription.request(1);
        }

        @Override
        public void onNext(String item) {
            System.out.println(name + ": " + item + " 수신");
            count.incrementAndGet();
            subscription.request(1);
        }

        @Override
        public void onError(Throwable throwable) {
            System.err.println(name + ": 에러 발생 - " + throwable.getMessage());
        }

        @Override
        public void onComplete() {
            System.out.println(name + ": 구독 완료, 총 수신: " + count.get() + "개");
        }

        public int getCount() {
            return count.get();
        }
    }

    /**
     * 메인 실행 예시
     */
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Java 9 Flow API 예시 ===\n");

        // 예시 1: 기본 Publisher/Subscriber
        System.out.println("1. 기본 Publisher/Subscriber 예시");
        NumberPublisher publisher = new NumberPublisher();
        NumberSubscriber subscriber = new NumberSubscriber("Subscriber-1");
        
        publisher.getPublisher().subscribe(subscriber);
        publisher.publishNumbers(5);
        
        Thread.sleep(100); // 비동기 처리 대기
        
        System.out.println("\n2. 여러 Subscriber 구독 예시");
        NumberPublisher publisher2 = new NumberPublisher();
        NumberSubscriber subscriber1 = new NumberSubscriber("Subscriber-A");
        NumberSubscriber subscriber2 = new NumberSubscriber("Subscriber-B");
        
        publisher2.getPublisher().subscribe(subscriber1);
        publisher2.getPublisher().subscribe(subscriber2);
        publisher2.publishNumbers(3);
        
        Thread.sleep(100);
        
        System.out.println("\n3. 배압 제어 예시");
        NumberPublisher publisher3 = new NumberPublisher();
        BufferedSubscriber bufferedSubscriber = new BufferedSubscriber(3);
        
        publisher3.getPublisher().subscribe(bufferedSubscriber);
        publisher3.publishNumbers(10);
        
        Thread.sleep(100);
        
        System.out.println("\n4. Processor를 통한 데이터 변환 예시");
        NumberPublisher publisher4 = new NumberPublisher();
        TransformProcessor processor = new TransformProcessor(
            num -> "숫자: " + num + " (제곱: " + (num * num) + ")"
        );
        StringSubscriber finalSubscriber = new StringSubscriber("Final-Subscriber");
        
        publisher4.getPublisher().subscribe(processor);
        processor.subscribe(finalSubscriber);
        publisher4.publishNumbers(5);
        
        Thread.sleep(200);
        
        System.out.println("\n=== 예시 완료 ===");
    }
}

