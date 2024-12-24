package org.example.testgen_cr.method_searcher;

import java.util.List;

import org.jtool.cfg.CCFG;
import org.jtool.cfg.CFG;
import org.jtool.jxplatform.builder.ModelBuilder;
import org.jtool.srcmodel.JavaMethod;
import org.jtool.srcmodel.JavaProject;

public class CalleeMethod {
    private JavaMethod callerMethod;
    private JavaMethod targetMethod;

    private List<ProjectVariable> inVariables;
    private List<ProjectVariable> outVariables;

    public CalleeMethod(MethodSeq methodSeq) {
        callerMethod = methodSeq.caller();
        targetMethod = methodSeq.callee();

        JavaProject targetProject = targetMethod.getJavaProject();
        ModelBuilder builder = targetProject.getModelBuilder();
        CCFG ccfg = builder.getCCFG(targetMethod.getDeclaringClass());
        CFG cfg = ccfg.getCFG(targetMethod.getQualifiedName().fqn());

        VariableFinder variableFinder = new VariableFinder(targetProject, targetMethod, cfg);
        this.inVariables = variableFinder.getInVariables();
        this.outVariables = variableFinder.getOutVariables();
    }

    public JavaMethod getCallerMethod() {
        return callerMethod;
    }

    public JavaMethod getTargetMethod() {
        return targetMethod;
    }

    public List<ProjectVariable> getInVariables() {
        return inVariables;
    }

    public List<ProjectVariable> getOutVariables() {
        return outVariables;
    }
}
