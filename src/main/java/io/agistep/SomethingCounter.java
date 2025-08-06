package io.agistep;

public class SomethingCounter {

    // 횟수
    int count = 0;

    public synchronized int add(int t) {
        count = count + t;
        return count;
    }

    public void printCount() {
        System.out.println("모든 횟수: " + count);
    }
}
