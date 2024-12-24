package org.example.testgen_cr.results;

import org.example.testgen_cr.model_buffs.TestGenMethod;
import org.example.testgen_cr.model_buffs.TestGenMethodSeq;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

public class ResultMethodSeq {
    private List<ResultMethod> methodChain = new ArrayList<>();
    private int seqId;
    private ZonedDateTime testingTime;

    private List<ResultVariable> inVars = new ArrayList<>();
    private List<ResultVariable> outVars = new ArrayList<>();
    private String newTestClassName;

    ResultMethodSeq(TestGenMethodSeq seq, ZonedDateTime time) {
        for (TestGenMethod tmethod : seq.methodChain()) {
            ResultMethod rmethod = new ResultMethod(tmethod);
            methodChain.add(rmethod);
        }
        this.seqId = seq.getId();
        this.testingTime = time;
    }

}
