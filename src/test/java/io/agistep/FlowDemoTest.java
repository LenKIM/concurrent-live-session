package io.agistep;

import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Flow;
import java.util.concurrent.SubmissionPublisher;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class FlowDemoTest {

    @Test
    void basicPublisherSubscriberTest() throws InterruptedException {
        // Given
        AtomicInteger receivedCount = new AtomicInteger(0);
        AtomicInteger sum = new AtomicInteger(0);
        CountDownLatch latch = new CountDownLatch(1);

        Flow.Subscriber<Integer> subscriber = new Flow.Subscriber<Integer>() {
            private Flow.Subscription subscription;

            @Override
            public void onSubscribe(Flow.Subscription subscription) {
                this.subscription = subscription;
                subscription.request(1);
            }

            @Override
            public void onNext(Integer item) {
                receivedCount.incrementAndGet();
                sum.addAndGet(item);
                subscription.request(1);
            }

            @Override
            public void onError(Throwable throwable) {
                latch.countDown();
            }

            @Override
            public void onComplete() {
                latch.countDown();
            }
        };


        SubmissionPublisher<Integer> publisher = new SubmissionPublisher<>();
        publisher.subscribe(subscriber);

        // When
        for (int i = 1; i <= 10; i++) {
            publisher.submit(i);
        }
        publisher.close();

        // Then
        boolean completed = latch.await(1, TimeUnit.SECONDS);
        assertThat(completed).isTrue();
        assertThat(receivedCount.get()).isEqualTo(10);
        assertThat(sum.get()).isEqualTo(55); // 1+2+...+10 = 55
    }

    @Test
    void multipleSubscribersTest() throws InterruptedException {
        // Given
        SubmissionPublisher<Integer> publisher = new SubmissionPublisher<>();
        AtomicInteger subscriber1Count = new AtomicInteger(0);
        AtomicInteger subscriber2Count = new AtomicInteger(0);
        CountDownLatch latch = new CountDownLatch(2);

        Flow.Subscriber<Integer> subscriber1 = createCountingSubscriber("Sub1", subscriber1Count, latch);
        Flow.Subscriber<Integer> subscriber2 = createCountingSubscriber("Sub2", subscriber2Count, latch);

        publisher.subscribe(subscriber1);
        publisher.subscribe(subscriber2);

        // When
        for (int i = 1; i <= 5; i++) {
            publisher.submit(i);
        }
        publisher.close();

        // Then
        boolean completed = latch.await(1, TimeUnit.SECONDS);
        assertThat(completed).isTrue();
        assertThat(subscriber1Count.get()).isEqualTo(5);
        assertThat(subscriber2Count.get()).isEqualTo(5);
    }

    @Test
    void backpressureTest() throws InterruptedException {
        // Given
        SubmissionPublisher<Integer> publisher = new SubmissionPublisher<>();
        AtomicInteger receivedCount = new AtomicInteger(0);
        CountDownLatch latch = new CountDownLatch(1);
        int bufferSize = 3;

        Flow.Subscriber<Integer> subscriber = new Flow.Subscriber<Integer>() {
            private Flow.Subscription subscription;

            @Override
            public void onSubscribe(Flow.Subscription subscription) {
                this.subscription = subscription;
                // 처음에 버퍼 크기만큼 요청
                subscription.request(bufferSize);
            }

            @Override
            public void onNext(Integer item) {
                receivedCount.incrementAndGet();
                // 버퍼가 비면 다시 요청
                if (receivedCount.get() % bufferSize == 0) {
                    subscription.request(bufferSize);
                }
            }

            @Override
            public void onError(Throwable throwable) {
                latch.countDown();
            }

            @Override
            public void onComplete() {
                latch.countDown();
            }
        };

        publisher.subscribe(subscriber);

        // When - 10개의 아이템 발행
        for (int i = 1; i <= 10; i++) {
            publisher.submit(i);
        }
        publisher.close();

        // Then
        boolean completed = latch.await(1, TimeUnit.SECONDS);
        assertThat(completed).isTrue();
        assertThat(receivedCount.get()).isEqualTo(10);
    }

    @Test
    void processorTest() throws InterruptedException {
        // Given
        SubmissionPublisher<Integer> sourcePublisher = new SubmissionPublisher<>();
        SubmissionPublisher<String> transformPublisher = new SubmissionPublisher<>();
        
        AtomicInteger transformedCount = new AtomicInteger(0);
        CountDownLatch latch = new CountDownLatch(1);

        // Processor 역할을 하는 Subscriber
        Flow.Subscriber<Integer> processor = new Flow.Subscriber<Integer>() {
            private Flow.Subscription subscription;

            @Override
            public void onSubscribe(Flow.Subscription subscription) {
                this.subscription = subscription;
                subscription.request(1);
            }

            @Override
            public void onNext(Integer item) {
                // 데이터 변환: 숫자를 문자열로 변환
                String transformed = "값: " + (item * 2);
                transformPublisher.submit(transformed);
                subscription.request(1);
            }

            @Override
            public void onError(Throwable throwable) {
                transformPublisher.closeExceptionally(throwable);
                latch.countDown();
            }

            @Override
            public void onComplete() {
                transformPublisher.close();
                latch.countDown();
            }
        };

        // 최종 Subscriber
        Flow.Subscriber<String> finalSubscriber = new Flow.Subscriber<String>() {
            private Flow.Subscription subscription;

            @Override
            public void onSubscribe(Flow.Subscription subscription) {
                this.subscription = subscription;
                subscription.request(1);
            }

            @Override
            public void onNext(String item) {
                transformedCount.incrementAndGet();
                subscription.request(1);
            }

            @Override
            public void onError(Throwable throwable) {
                // 에러 처리
            }

            @Override
            public void onComplete() {
                // 완료 처리
            }
        };

        sourcePublisher.subscribe(processor);
        transformPublisher.subscribe(finalSubscriber);

        // When
        for (int i = 1; i <= 5; i++) {
            sourcePublisher.submit(i);
        }
        sourcePublisher.close();

        // Then
        boolean completed = latch.await(1, TimeUnit.SECONDS);
        assertThat(completed).isTrue();
        assertThat(transformedCount.get()).isEqualTo(5);
    }

    private Flow.Subscriber<Integer> createCountingSubscriber(
            String name, 
            AtomicInteger counter, 
            CountDownLatch latch) {
        return new Flow.Subscriber<Integer>() {
            private Flow.Subscription subscription;

            @Override
            public void onSubscribe(Flow.Subscription subscription) {
                this.subscription = subscription;
                subscription.request(1);
            }

            @Override
            public void onNext(Integer item) {
                counter.incrementAndGet();
                subscription.request(1);
            }

            @Override
            public void onError(Throwable throwable) {
                latch.countDown();
            }

            @Override
            public void onComplete() {
                latch.countDown();
            }
        };
    }
}

