package org.example.testgen_cr;

import org.example.testgen_cr.analyzer.MethodAnalyzer;
import org.example.testgen_cr.evosuite.EvosuiteRunner;
import org.example.testgen_cr.evosuite.EvosuiteTestRunner;
import org.example.testgen_cr.exception.TestGenException;
import org.example.testgen_cr.filter.MethodFilter;
import org.example.testgen_cr.generater.Generater;
import org.example.testgen_cr.instrumenter.Compiler;
import org.example.testgen_cr.instrumenter.LogCodeInserter;
import org.example.testgen_cr.model.TestGenMethod;
import org.example.testgen_cr.model.TestGenMethodSeq;
import org.example.testgen_cr.model.TestGenParts;
import org.jtool.srcmodel.JavaProject;

import java.util.List;

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

//            FilterResult filterResult = methodFilter.getFilterResult();

            System.out.println("FilterResult" + testTargetMethodSeqList);
            int i = 0;

            for (TestGenMethodSeq jmethodSeq : testTargetMethodSeqList) {
                try {
                    System.out.println(i);
                    i++;
                    if (i == 10) {
                        break;
                    }
                    if (i != 3) {
                        continue;
                    }
                    JavaProject targetProject = jmethodSeq.getJavaProject();
                    TestGenMethod targetMethod = jmethodSeq.getTestGenTargetMethod();
                    TestGenMethod evoTargetMethod = jmethodSeq.getCallerMethod(targetMethod);

                    if (!Compiler.run(evoTargetMethod.getJavaMethod().getFile(), targetProject)) {
                        throw new TestGenException("Could not compile");
                    }

                    EvosuiteRunner evosuiteRunner = new EvosuiteRunner(jmethodSeq);
                    TestGenMethodSeq evoMethodSeq = evosuiteRunner.run(evoTargetMethod);

                    if (evoMethodSeq == null) {
                        throw new TestGenException("Could not evosuite");
                    }

                    LogCodeInserter inserter = new LogCodeInserter();
                    if (inserter.insertLogCode(evoMethodSeq, targetProject)) {
                        System.out.println("insertLogCode()");
                        EvosuiteTestRunner runner = new EvosuiteTestRunner();
                        runner.compile(evoMethodSeq);
                        System.out.println("compile()");

                        List<TestGenParts> testGenPartsList = runner.runEvoTest(evoMethodSeq, targetProject);
                        System.out.println("runEvoTests(): " + testGenPartsList.size());

                        for (TestGenParts testGenParts : testGenPartsList) {
                            System.out.println(Generater.generate(testGenParts));
                        }
                    } else {
                        System.out.println("insertLogCode() error");
                    }

                } catch (Exception e) {
                    System.err.println("Error during test generation process: " + e);
                }
            }

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

    public static void main(String[] args) {
        TestGen testGen = new TestGen(args);
        testGen.run2();
    }
}
