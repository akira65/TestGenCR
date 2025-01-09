package org.example.testgen_cr;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.example.testgen_cr.analyzer.MethodAnalyzer;
import org.example.testgen_cr.exception.TestGenException;
import org.example.testgen_cr.filter.MethodFilter;
import org.example.testgen_cr.model.TestGenMethodSeq;
import org.jtool.cfg.CFG;
import org.jtool.cfg.CFGNode;
import org.jtool.cfg.CFGStatement;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class TestGen {

    private String targetProjectPath;
    private boolean logging = false;
    private int callChainNum = 1; // 3����のコールチェーンで��り込む
    private int minLoc = 1;

    public TestGen(String[] args) {
        this.targetProjectPath = "/Users/hara_ko/Desktop/TestGenCR/testGenTarget/Java";
    }

    public void run2() {
        try {
            // 1. ターゲットプロジェクト解析
            MethodAnalyzer analyzer = new MethodAnalyzer(logging);
            List<TestGenMethodSeq> methodSeqList = analyzer.analyze(targetProjectPath, callChainNum, minLoc);

//            // 2. メソッドペア条件に基づく絞り込み
            MethodFilter methodFilter = new MethodFilter(methodSeqList);
            List<TestGenMethodSeq> testTargetMethodSeqList = methodFilter.getTargetMethodSeqList();


            System.out.println(testTargetMethodSeqList.get(0).getCallerMethod(testTargetMethodSeqList.get(0).getTestGenTargetMethod()).getQualifiedName());
            CFG cfg = testTargetMethodSeqList.get(0).getCallerMethod(testTargetMethodSeqList.get(0).getTestGenTargetMethod()).getCfg();

            Set<CFGNode> sliceNodes = new HashSet<>();
            System.out.println("111");
            CFGStatement targetNode = null;

            for (CFGStatement statement : cfg.getStatementNodes()) {
                if (statement.getASTNode() instanceof MethodInvocation) {
                    MethodInvocation methodInvocation = (MethodInvocation) statement.getASTNode();
                    if (methodInvocation.getName().getIdentifier().equals("partition")) {
                        targetNode = statement;
                        break;
                    }
                }
            }

            if (targetNode == null) {
                throw new TestGenException("Target method not found in CFG.");
            }

            sliceNodes.addAll(findDependencyNodes(cfg, targetNode));
            if (isInsideBlock(targetNode)) {
                handleBlockExit(targetNode, sliceNodes);
            }

            Set<String> codeLines = new HashSet<>();
            System.out.println("================================================================");
            for (CFGNode node : sliceNodes) {
                if (node instanceof CFGStatement) {
                    ASTNode astNode = ((CFGStatement) node).getASTNode();
                    if (astNode != null) {
                        String sourceCode = astNode.toString().trim();
                        codeLines.add(sourceCode);
                    }
                }
            }
            System.out.println(codeLines.stream().sorted().collect(Collectors.joining(System.lineSeparator())));
//
////            FilterResult filterResult = methodFilter.getFilterResult();
//
//            System.out.println("FilterResult" + testTargetMethodSeqList);
//            int i = 0;
//
//            for (TestGenMethodSeq jmethodSeq : testTargetMethodSeqList) {
//                try {
//                    System.out.println(i);
//                    i++;
//                    if (i == 10) {
//                        break;
//                    }
//                    if (i != 3) {
//                        continue;
//                    }
//                    JavaProject targetProject = jmethodSeq.getJavaProject();
//                    TestGenMethod targetMethod = jmethodSeq.getTestGenTargetMethod();
//                    TestGenMethod evoTargetMethod = jmethodSeq.getCallerMethod(targetMethod);
//
//                    if (!Compiler.run(evoTargetMethod.getJavaMethod().getFile(), targetProject)) {
//                        throw new TestGenException("Could not compile");
//                    }
//
//                    EvosuiteRunner evosuiteRunner = new EvosuiteRunner(jmethodSeq);
//                    TestGenMethodSeq evoMethodSeq = evosuiteRunner.run(evoTargetMethod);
//
//                    if (evoMethodSeq == null) {
//                        throw new TestGenException("Could not evosuite");
//                    }
//
//                    LogCodeInserter inserter = new LogCodeInserter();
//                    if (inserter.insertLogCode(evoMethodSeq, targetProject)) {
//                        System.out.println("insertLogCode()");
//                        EvosuiteTestRunner runner = new EvosuiteTestRunner();
//                        runner.compile(evoMethodSeq);
//                        System.out.println("compile()");
//
//                        List<TestGenParts> testGenPartsList = runner.runEvoTest(evoMethodSeq, targetProject);
//                        System.out.println("runEvoTests(): " + testGenPartsList.size());
//
//                        for (TestGenParts testGenParts : testGenPartsList) {
//                            System.out.println(Generater.generate(testGenParts));
//                        }
//                    } else {
//                        System.out.println("insertLogCode() error");
//                    }
//
//                } catch (Exception e) {
//                    System.err.println("Error during test generation process: " + e);
//                }
//            }

//            System.out.println("Filtered methods: " + filterResult.getNotFoundInputVariables());
//            System.out.println(filterResult.getNotFoundInputVariables().size());
//            filterResult.getIsNotObjectInput().forEach(aa -> System.out.println(aa.substring(aa.indexOf("("), aa.indexOf("@"))));
//            filterResult.getIsNotPrimitiveOutput().forEach(aa -> System.out.println(aa.split("@")[1]));
//            // 3. TestTargetの生成（1つまたは複数のシーケンスに基づいて生成できるようにする）
//            // ここでは例として最初のメソッドシーケンスをターゲットとする
//            TestTarget testTarget = new TestTarget(filteredSeqs.get(0).methodChain().get(0).getJavaMethod());

            // 今後の処理はtestTargetやfilteredSeqsを元にTestGeneratorやEvoSuiteRunnerを呼び出す流れ
            // 例）TestGenerator generator = new TestGenerator(...);
            //     generator.generate(...);

        } catch (TestGenException e) {
            System.err.println("Error during test generation process: " + e);
//        }
//        catch (org.example.testgen_cr.TestGenException e) {
//            System.err.println("Error during test generation process: " + e);
        } catch (Exception e) {
            System.out.println("Error: " + e);
        }
    }

//    private static Set<CFGNode> findDependencyNodes(CFG cfg, CFGStatement targetNode) {
//        List<String> variables = Arrays.asList("array", "low", "high");
//        Set<CFGNode> dependencyNodes = new HashSet<>();
//
//        for (String variable : variables) {
//            for (CFGStatement statement : cfg.getStatementNodes()) {
//                if (statement.toString().contains(variable)) {
//                    dependencyNodes.add(statement);
//                }
//                if (targetNode.equals(statement)) {
//                    return dependencyNodes;
//                }
//            }
//        }
//        return dependencyNodes;
//    }

    private static Set<CFGNode> findDependencyNodes(CFG cfg, CFGStatement targetNode) {
        List<String> variables = Arrays.asList("array", "low", "high");
        Set<CFGNode> dependencyNodes = new HashSet<>();

        for (String variable : variables) {
            for (CFGStatement statement : cfg.getStatementNodes()) {
                // 変数の正確な一致をチェック
                if (statement.getDefVariables().stream().anyMatch(v -> v.getName().equals(variable))) {
                    dependencyNodes.add(statement);
                }
            }
        }
        return dependencyNodes;
    }


    private static boolean isInsideBlock(CFGStatement node) {
        return node.getASTNode().getParent() instanceof org.eclipse.jdt.core.dom.Block;
    }

    private static void handleBlockExit(CFGNode node, Set<CFGNode> sliceNodes) {
        CFGNode blockExitNode = findBlockExitNode(node);
        if (blockExitNode != null) {
            sliceNodes.add(blockExitNode);
        }
    }

    private static CFGNode findBlockExitNode(CFGNode node) {
        return node.getSuccessors().stream().filter(successor -> !(successor.getASTNode() instanceof org.eclipse.jdt.core.dom.Block)).findFirst().orElse(null);
    }

    public static void main(String[] args) {
        TestGen testGen = new TestGen(args);
        testGen.run2();
    }
}
