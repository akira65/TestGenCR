package org.example.testgen_cr.model;

import org.example.testgen_cr.TestGenException;
import org.example.testgen_cr.results.ResultMethodSeq;
import org.example.testgen_cr.searcher.MethodFinder;
import org.jtool.pdg.DependencyGraph;
import org.jtool.srcmodel.JavaClass;
import org.jtool.srcmodel.JavaField;
import org.jtool.srcmodel.JavaMethod;
import org.jtool.srcmodel.JavaProject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TestTarget {
    private static final int CALLING_CHAIN_LIMIT = 1;

    private final JavaProject targetProject;
    private final JavaMethod targetMethod;
    private List<TestGenMethodSeq> methodSeqList;
    private final List<TestGenMethod> testMethods = new ArrayList<>();
    private final Set<JavaClass> testClasses = new HashSet<>();
    private final Set<JavaMethod> usedMethods = new HashSet<>();
    private final Set<JavaField> usedFields = new HashSet<>();

    private List<TestGenVariable> inVariables = new ArrayList<>();
    private List<TestGenVariable> outVariables = new ArrayList<>();

    private List<ResultMethodSeq> resultingMethodSeqList = new ArrayList<>();
    private DependencyGraph dependencyGraph = null;

    public TestTarget(JavaMethod targetMethod) throws TestGenException {
        this(targetMethod, CALLING_CHAIN_LIMIT);
    }

    public TestTarget(JavaMethod targetMethod, int chain_num) throws TestGenException {
        this.targetProject = targetMethod.getJavaProject();
        this.targetMethod = targetMethod;

        MethodFinder methodFinder = new MethodFinder(targetProject.getModelBuilder());
        List<TestGenMethodSeq> callingMethodList = methodFinder.collectCallingMethodSeqList(targetMethod, chain_num);
//        createTestTarget(callingMethodList);
//        collectUsedMembersInOriginal();
    }
}
