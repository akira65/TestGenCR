package org.example.testgen_cr.model_buffs;

import java.util.Comparator;
import java.util.List;

public class TestGenMethodSeq {

    private List<TestGenMethod> methodChain;
    private TestGenMethod methodInTestClass;
    private int id;

    public TestGenMethodSeq(List<TestGenMethod> methodChain, TestGenMethod tmethod) {
        this.methodChain = methodChain;
        this.methodInTestClass = tmethod;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public TestGenMethod getMethodInTestClass() {
        return methodInTestClass;
    }

    public List<TestGenMethod> methodChain() {
        return methodChain;
    }

    public String toString2() {
        StringBuilder buf = new StringBuilder();
        buf.append(methodChain.get(0).getQualifiedName2());
        for (int index = 1; index < methodChain.size(); index++) {
            buf.append(" <- " + methodChain.get(index).getQualifiedName2());
        }
        return buf.toString();
    }

    public static void sort(List<? extends TestGenMethodSeq> seq) {
        seq.sort(new Comparator<TestGenMethodSeq>() {

            @Override
            public int compare(TestGenMethodSeq seq1, TestGenMethodSeq seq2) {
                return seq1.getId() - seq2.getId();
            }
        });
    }
}
