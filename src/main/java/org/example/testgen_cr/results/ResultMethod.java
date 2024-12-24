package org.example.testgen_cr.results;

import org.example.testgen_cr.model_buffs.TestGenMethod;
import org.jtool.cfg.JReference;
import org.jtool.pdg.PDGNode;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ResultMethod {
    protected TestGenMethod testGenMethod;

    private Set<PDGNode> extractedNodes = new HashSet<>();
    private List<JReference> parameters = new ArrayList<>();

    private String newName;
    private String receiverName = null;
    private String receiverType = null;

    public ResultMethod(TestGenMethod tmethod) {
        this.testGenMethod = tmethod;
        newName = tmethod.getJavaMethod().getName();
    }

}
