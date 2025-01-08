package org.example.testgen_cr.model;

import org.jtool.srcmodel.JavaProject;

import java.io.File;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class TestGenMethodSeq {
    private static final AtomicInteger ID_GENERATOR = new AtomicInteger(0); // スレッドセーフなカウンター

    private final List<TestGenMethod> methodChain;
    private int id;
    private ZonedDateTime testingTime;
    private TestGenMethod evoTargetMethod;
    private String evoTestPath;
    private String evoTestBinPath;

    public TestGenMethodSeq(List<TestGenMethod> methodChain) {
        this.methodChain = methodChain;
        this.id = ID_GENERATOR.incrementAndGet();
    }

    public TestGenMethodSeq createTargetMethodSeq(TestGenMethod callerMethod) {
        List<TestGenMethod> targetMethodChain = getTargetMethodChain(callerMethod);
        return new TestGenMethodSeq(targetMethodChain);
    }

    public void setEvoTargetMethod(TestGenMethod targetMethod) {
        this.evoTargetMethod = targetMethod;
    }

    public TestGenMethod getEvoTargetMethod() {
        return evoTargetMethod;
    }

    public void setEvoTestPath(String testPath) {
        this.evoTestPath = testPath;
//        this.evoTestBinPath = testPath.replace(File.separatorChar + "src" + File.separatorChar, File.separatorChar + "classes" + File.separatorChar);
        this.evoTestBinPath = testPath.replace(File.separatorChar + "src", File.separatorChar + "classes");

    }

    public String getEvoTestPath() {
        return evoTestPath;
    }

    public String getEvoTestBinPath() {
        return evoTestBinPath;
    }

    private List<TestGenMethod> getTargetMethodChain(TestGenMethod callerMethod) {
        List<TestGenMethod> methodChain = getMethodChain();
        int callerMethodIndex = methodChain.indexOf(callerMethod);

        if (callerMethodIndex != -1) {
            return methodChain.subList(0, callerMethodIndex + 1);
        }
        return new ArrayList<>();
    }

    public List<TestGenMethod> getMethodChain() {
        return methodChain;
    }

    public int getMethodChainSize() {
        return methodChain.size();
    }

    public TestGenMethod getTestGenTargetMethod() {
        return methodChain.get(0);
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
        sb.append(methodChain.get(0).getQualifiedNameWithCalling());
        for (int i = 1; i < methodChain.size(); i++) {
            sb.append(" <- ").append(methodChain.get(i).getQualifiedNameWithCalling());
        }
        return sb.toString();
    }

    public JavaProject getJavaProject() {
        return methodChain.get(0).getJavaProject();
    }

    public static void sort(List<? extends TestGenMethodSeq> seqs) {
        seqs.sort(Comparator.comparingInt(TestGenMethodSeq::getId));
    }
}
