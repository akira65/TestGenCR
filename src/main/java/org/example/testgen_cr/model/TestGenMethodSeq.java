package org.example.testgen_cr.model;

import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class TestGenMethodSeq {
    private static final AtomicInteger ID_GENERATOR = new AtomicInteger(0); // スレッドセーフなカウンター

    private final List<TestGenMethod> methodChain;
    private int id;
    private ZonedDateTime testingTime;

    public TestGenMethodSeq(List<TestGenMethod> methodChain) {
        this.methodChain = methodChain;
        this.id = ID_GENERATOR.incrementAndGet();
    }

    public List<TestGenMethod> getMethodChain() {
        return methodChain;
    }

    public int getMethodChainSize() {
        return methodChain.size();
    }

    public TestGenMethod getTestGenTargetMethod() {
        return methodChain.getFirst();
    }

    public int getSeqIndex(TestGenMethod method) {
        return methodChain.indexOf(method);
    }

    public TestGenMethod getCalleeMethod(TestGenMethod method) {
        int calleeMethodIndex = getSeqIndex(method) - 1;
        if (calleeMethodIndex < 0) {
            return null;
        }
        return methodChain.get(calleeMethodIndex);
    }

    public TestGenMethod getCallerMethod(TestGenMethod method) {
        int callerMethodIndex = getSeqIndex(method) + 1;
        if (callerMethodIndex >= methodChain.size()) {
            return null;
        }
        return methodChain.get(callerMethodIndex);
    }

    public TestGenMethod getSeqMethod(int index) {
        return methodChain.get(index);
    }

    public int getId() {
        return id;
    }

    public void setTestingTime(ZonedDateTime testingTime) {
        this.testingTime = testingTime;
    }

    public ZonedDateTime getTestingTime() {
        return testingTime;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(methodChain.getFirst().getQualifiedNameWithCalling());
        for (int i = 1; i < methodChain.size(); i++) {
            sb.append(" <- ").append(methodChain.get(i).getQualifiedNameWithCalling());
        }
        return sb.toString();
    }

    public static void sort(List<? extends TestGenMethodSeq> seqs) {
        seqs.sort(Comparator.comparingInt(TestGenMethodSeq::getId));
    }
}
