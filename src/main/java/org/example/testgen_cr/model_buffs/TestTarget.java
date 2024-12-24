package org.example.testgen_cr.model_buffs;


import org.example.testgen_cr.TestGenException;
import org.example.testgen_cr.results.ResultMethodSeq;
import org.jtool.cfg.CCFG;
import org.jtool.cfg.CFG;
import org.jtool.jxplatform.builder.ModelBuilder;
import org.jtool.pdg.DependencyGraph;
import org.jtool.srcmodel.*;

import java.util.*;
import java.util.stream.Collectors;

public class TestTarget {

    private static final int CALLING_CHAIN_LIMIT = 1;

    private JavaProject targetProject;
    private JavaMethod targetMethod;
    private List<TestGenMethodSeq> methodSeqList;
    private List<TestGenMethod> testMethods = new ArrayList<>();
    private Set<JavaClass> testClasses = new HashSet<>();
    private Set<JavaFile> testFiles = new HashSet<>();

    private Set<JavaClass> usedClasses = new HashSet<>();
    private Set<JavaMethod> usedMethods = new HashSet<>();
    private Set<JavaField> usedFields = new HashSet<>();

    private List<TestGenVariable> inVariables = new ArrayList<>();
    private List<TestGenVariable> outVariables = new ArrayList<>();

    private List<ResultMethodSeq> resultingMethodSeqList = new ArrayList<>();
    private Set<JavaClass> usedClassesInResults = null;
    private Set<JavaMethod> usedMethodsInResults = null;
    private Set<JavaField> usedFieldsInResults = null;

    private DependencyGraph dependencyGraph = null;

    public TestTarget(JavaMethod targetMethod) throws TestGenException {
        this(targetMethod, CALLING_CHAIN_LIMIT);
    }

    public TestTarget(JavaMethod targetMethod, int count) throws TestGenException {
        this.targetProject = targetMethod.getJavaProject();
        this.targetMethod = targetMethod;

        MethodFinder methodFinder = new MethodFinder(targetProject.getModelBuilder());
        List<TestGenMethodSeq> callingMethods = methodFinder.collectCallingMethodSeqList(targetMethod, count);
        createTestTarget(callingMethods);
        collectUsedMembersInOriginal();
    }

    private void createTestTarget(List<TestGenMethodSeq> callingMethods) throws TestGenException {
        methodSeqList = new ArrayList<>();
        Map<String, TestGenMethodSeq> seqMap = new HashMap<>();

        for (TestGenMethodSeq testMethodSeq : callingMethods) {
            String seqId = targetMethod.getQualifiedName() + "-" + testMethodSeq.getMethodInTestClass().getQualifiedName2();
            TestGenMethodSeq seq = seqMap.get(seqId);

            if (seq == null) {
                seqMap.put(seqId, testMethodSeq);
                methodSeqList.add(testMethodSeq);
            } else {
                methodSeqList.clear();
                throw new TestGenException("Multiple paths were found.");
            }
        }

        int id = 0;
        for (TestGenMethodSeq seq : methodSeqList) {
            TestGenMethod tmethod = seq.getMethodInTestClass();
            testMethods.add(tmethod);
            testClasses.add(tmethod.getJavaClass());
            testFiles.add(tmethod.getJavaClass().getFile());

            seq.setId(id);
            id++;
        }
    }

    private void collectUsedMembersInOriginal() {
        Set<JavaMethod> origins = new HashSet<>();
        origins.add(targetMethod);
        for (TestGenMethodSeq seq : methodSeqList) {
            seq.methodChain().forEach(jm -> origins.add(jm.getJavaMethod()));

            JavaClass testClass = seq.getMethodInTestClass().getJavaClass();
            testClass.getMethods()
                    .stream()
                    .filter(jm -> jm.isInitializer() || TestGenMethod.isBeforeOrAfterTestMethod(jm))
                    .forEach(jm -> origins.add(jm));
        }

        collectUsedMembers(origins, usedClasses, usedMethods, usedFields);
    }

    private void collectUsedMembers(Set<JavaMethod> origins, Set<JavaClass> classes, Set<JavaMethod> methods, Set<JavaField> fields) {
        for (JavaMethod jmethod : origins) {
            methods.add(jmethod);
            classes.add(jmethod.getDeclaringClass());

            for (JavaMethod jm : jmethod.getCalledMethodsInProject()) {
                if (!origins.contains(jm)) {
                    collectMembersRelatedTo(jm, classes, methods, fields);
                }
            }

            for (JavaField jf : jmethod.getAccessedFieldsInProject()) {
                if (jf.getFile().equals(jmethod.getFile())) {
                    collectMembersRelatedTo(jf, classes, methods, fields);
                }
            }
        }
    }

    private void collectMembersRelatedTo(JavaMethod jmethod, Set<JavaClass> classes, Set<JavaMethod> methods, Set<JavaField> fields) {
        if (methods.contains(jmethod)) {
            return;
        }
        methods.add(jmethod);
        classes.add(jmethod.getDeclaringClass());

//        caller, calleeが同じファイル内にあるかどうか
        for (JavaMethod jm : jmethod.getCalledMethodsInProject()) {
            if (jm.getFile().equals(jmethod.getFile())) {
                collectMembersRelatedTo(jm, classes, methods, fields);
            } else {
                methods.add(jm);
                classes.add(jm.getDeclaringClass());
            }
        }

        for (JavaField jf : jmethod.getAccessedFieldsInProject()) {
            if (jf.getFile().equals(jmethod.getFile())) {
                collectMembersRelatedTo(jf, classes, methods, fields);
            } else {
                fields.add(jf);
                classes.add(jf.getDeclaringClass());
            }
        }
    }

    private void collectMembersRelatedTo(JavaField jfield, Set<JavaClass> classes, Set<JavaMethod> methods, Set<JavaField> fields) {
        if (fields.contains(jfield)) {
            return;
        }
        fields.add(jfield);
        classes.add(jfield.getDeclaringClass());

        for (JavaMethod jm : jfield.getCalledMethodsInProject()) {
            if (jm.getFile().equals(jfield.getFile())) {
                collectMembersRelatedTo(jm, classes, methods, fields);
            } else {
                methods.add(jm);
                classes.add(jm.getDeclaringClass());
            }
        }
        for (JavaField jf : jfield.getAccessedFieldsInProject()) {
            if (jf.getFile().equals(jfield.getFile())) {
                collectMembersRelatedTo(jf, classes, methods, fields);
            } else {
                fields.add(jf);
                classes.add(jf.getDeclaringClass());
            }
        }
    }

    public void collectTestInfo() {
        ModelBuilder builder = targetProject.getModelBuilder();
        CCFG ccfg = builder.getCCFG(targetMethod.getDeclaringClass()); // クラス用
        CFG cfg = ccfg.getCFG(targetMethod.getQualifiedName().fqn()); // メソッド用

        VariableFinder variableFinder = new VariableFinder(targetProject, targetMethod, cfg);

        this.inVariables = variableFinder.getInVariables();
        this.outVariables = variableFinder.getOutVariables();
    }

    public Set<JavaFile> getRelatedFiles() {
        Set<JavaFile> files = new HashSet<JavaFile>();
        files.add(getTargetJavaClass().getFile());
        files.addAll(getTestJavaFiles());
        return files;
    }

    public List<TestGenMethodSeq> getMethodSeqList() {
        if (methodSeqList != null) {
            return methodSeqList;
        } else {
            return new ArrayList<>();
        }
    }

    public List<TestGenVariable> getInVariables() {
        return inVariables;
    }

    public List<TestGenVariable> getOutVariables() {
        return outVariables;
    }

    public JavaMethod getTargetJavaMethod() {
        return targetMethod;
    }

    public JavaClass getTargetJavaClass() {
        return targetMethod.getDeclaringClass();
    }

    public JavaProject getTargetJavaProject() {
        return targetProject;
    }

    public Set<JavaMethod> getTestJavaMethods() {
        return testMethods
                .stream()
                .map(method -> method.getJavaMethod())
                .distinct()
                .collect(Collectors.toSet());
    }

    public Set<JavaFile> getTestJavaFiles() {
        return testFiles;
    }

    public void sortTestGenVariableList() {
        TestGenVariable.sort(inVariables);
        TestGenVariable.sort(outVariables);
    }

    public void sortTestGenMethodSeqList() {
        TestGenMethodSeq.sort(methodSeqList);
    }
}
