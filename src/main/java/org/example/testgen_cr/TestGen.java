package org.example.testgen_cr;

import org.example.testgen_cr.exception.TestGenException;
import org.example.testgen_cr.searcher.MethodRelationshipAnalyzer;

public class TestGen {

    private String targetProjectPath;
    private boolean logging = true;
    private int callChainNum = 3; // 3����のコールチェーンで��り込む
    private int minLoc = 1;

    public TestGen(String[] args) {
        this.targetProjectPath = "/Users/hara_ko/Desktop/TestGenCR/testGenTarget/spark-master";
    }

    public void run2() {
        try {
            // 1. ターゲットプロジェクト解析
            MethodRelationshipAnalyzer analyzer = new MethodRelationshipAnalyzer(logging);
            analyzer.analyze(targetProjectPath, callChainNum, minLoc);
//
//            // 2. メソッドペア条件に基づく絞り込み
//            MethodPairFilter filter = new MethodPairFilter();
//            List<TestGenMethodSeq> filteredSeqs = filter.filter(allMethodSeqs,
//                    seq -> seq.hasReferenceTypeArguments() && seq.hasPrimitiveReturnValue());
//
//            if (filteredSeqs.isEmpty()) {
//                System.out.println("No method pairs matched the criteria.");
//                return;
//            }
//
//            // 3. TestTargetの生成（1つまたは複数のシーケンスに基づいて生成できるようにする）
//            // ここでは例として最初のメソッドシーケンスをターゲットとする
//            TestTarget testTarget = new TestTarget(filteredSeqs.get(0).methodChain().get(0).getJavaMethod());

            // 今後の処理はtestTargetやfilteredSeqsを元にTestGeneratorやEvoSuiteRunnerを呼び出す流れ
            // 例）TestGenerator generator = new TestGenerator(...);
            //     generator.generate(...);

        } catch (TestGenException e) {
            System.err.println("Error during test generation process: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        TestGen testGen = new TestGen(args);
        testGen.run2();
    }
}
