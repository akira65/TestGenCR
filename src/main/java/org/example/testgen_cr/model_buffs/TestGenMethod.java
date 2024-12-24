package org.example.testgen_cr.model_buffs;

import org.jtool.cfg.CFG;
import org.jtool.cfg.CFGMethodCall;
import org.jtool.srcmodel.JavaClass;
import org.jtool.srcmodel.JavaMethod;

public class TestGenMethod {

    private CFG cfg;
    private JavaMethod jmethod;
    private CFGMethodCall callNode;

    public TestGenMethod(CFG cfg, JavaMethod jmethod, CFGMethodCall callNode) {
        this.cfg = cfg;
        this.jmethod = jmethod;
        this.callNode = callNode;
    }

    public JavaClass getJavaClass() {
        return jmethod.getDeclaringClass();
    }

    public String getQualifiedName() {
        return jmethod.getQualifiedName().fqn();
    }

    public String getQualifiedName2() {
        return jmethod.getQualifiedName() + "@" + "{" + callNode.getASTNode().getStartPosition() + "}";
    }

    public JavaMethod getJavaMethod() {
        return jmethod;
    }

    public static boolean isBeforeOrAfterTestMethod(JavaMethod jmethod) {
        if (jmethod.getSignature().equals("setUp( )") || jmethod.getSignature().equals("tearDown( )")) {
            return true;
        } else {
            String anno = MethodFinder.getAnnotation(jmethod);
            if ("Before".equals(anno) || "BeforeClass".equals(anno) ||
                    "After".equals(anno) || "AfterClass".equals(anno)) {
                return true;
            }
        }
        return false;
    }
}
