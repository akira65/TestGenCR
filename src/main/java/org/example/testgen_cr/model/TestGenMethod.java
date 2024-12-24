package org.example.testgen_cr.model;

import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.example.testgen_cr.searcher.MethodFinder;
import org.jtool.cfg.CFG;
import org.jtool.cfg.CFGMethodCall;
import org.jtool.srcmodel.JavaMethod;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TestGenMethod {
    private final CFG cfg;
    private final JavaMethod jmethod;
    private final CFGMethodCall callNode;

    public TestGenMethod(CFG cfg, JavaMethod jmethod, CFGMethodCall callNode) {
        this.cfg = cfg;
        this.jmethod = jmethod;
        this.callNode = callNode;
    }

    public String getFullReturnType() {
        IMethodBinding methodBinding = this.jmethod.getMethodBinding();
        if (methodBinding == null) {
            return "void";
        }
        ITypeBinding returnTypeBinding = methodBinding.getReturnType();
        return returnTypeBinding.getQualifiedName();
    }

    public List<String> getFullVariableTypeList() {
        List<String> fullValiableTypeList = new ArrayList<String>();
        IMethodBinding methodBinding = this.jmethod.getMethodBinding();
        if (methodBinding == null) {
            return fullValiableTypeList;
        }

        ITypeBinding[] typeBindingList = methodBinding.getParameterTypes();
        fullValiableTypeList = Arrays.stream(typeBindingList).map(ITypeBinding::getQualifiedName).toList();
        return fullValiableTypeList;
    }

    private String aa(ITypeBinding a) {
        return a.getQualifiedName();
    }

    public CFG getCfg() {
        return cfg;
    }

    public JavaMethod getJavaMethod() {
        return jmethod;
    }

    public CFGMethodCall getCallNode() {
        return callNode;
    }

    public String getQualifiedName() {
        return jmethod.getQualifiedName().fqn();
    }

    public String getQualifiedNameWithCalling() {
        return jmethod.getQualifiedName() + (callNode != null ? "@" + "{" + callNode.getASTNode().getStartPosition() + "}" : "");
    }

    // テストメソッド判定
    public static boolean isBeforeOrAfterTestMethod(JavaMethod jmethod) {
        if (jmethod.getSignature().equals("setUp( )") || jmethod.getSignature().equals("tearDown( )")) {
            return true;
        } else {
            String anno = MethodFinder.getAnnotation(jmethod);
            return "Before".equals(anno) || "BeforeClass".equals(anno) ||
                    "After".equals(anno) || "AfterClass".equals(anno);
        }
    }
}
