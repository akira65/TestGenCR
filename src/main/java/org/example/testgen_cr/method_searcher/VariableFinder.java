package org.example.testgen_cr.method_searcher;

import org.example.testgen_cr.method_searcher.ProjectVariable.Direction;
import org.example.testgen_cr.method_searcher.ProjectVariable.Sort;
import org.jtool.cfg.*;
import org.jtool.srcmodel.JavaMethod;
import org.jtool.srcmodel.JavaProject;
import org.jtool.srcmodel.QualifiedName;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class VariableFinder {

    private Set<ProjectVariable> inVariables = new HashSet<>();
    private Set<ProjectVariable> outVariables = new HashSet<>();

    public VariableFinder(JavaProject targetProject, JavaMethod targetMethod, CFG cfg) {
        collectInVariables(targetProject, targetMethod, cfg);
        collectOutVariables(targetProject, targetMethod, cfg);
    }

    private void collectInVariables(JavaProject targetProject, JavaMethod targetMethod, CFG cfg) {
        Set<String> names = new HashSet<>();
//        いったん、フィールド変数は集めない
        if (targetMethod.isStatic()) {
            collectParametersOnly(cfg, names);
        }
    }

    private void collectParametersOnly(CFG cfg, Set<String> names) {
        for (CFGNode node : cfg.getNodes()) {
            if (node.isFormal()) {
                collectInParameters(cfg, node, names);
            }
        }
    }

    private void collectInParameters(CFG cfg, CFGNode node, Set<String> names) {
        CFGParameter paramNode = (CFGParameter) node;
        if (paramNode.isFormalIn()) {
            JVariableReference var = paramNode.getDefVariable();

            QualifiedName qname = new QualifiedName(var.getEnclosingClassName(), var.getEnclosingMethodName());
            String name = qname.fqn() + "!" + var.getName() + "$" + String.valueOf(paramNode.getIndex() + 1);
            names.add(name);

            String type = var.getType();
            boolean primitive = var.isPrimitiveType();
            int modifiers = var.getModifiers();
            String access = var.getName();

            ProjectVariable tvar = new ProjectVariable(cfg, paramNode, var,
                    Sort.PARAMETER, Direction.IN, name, type, primitive, modifiers, access, paramNode.getIndex());

            inVariables.add(tvar);

        }
    }

    private void collectOutVariables(JavaProject targetProject, JavaMethod targetMethod, CFG cfg) {
        Set<String> names = new HashSet<>();
        if (targetMethod.isStatic()) {
            collectReturnsOnly(targetProject, cfg, names);
        }
    }

    private void collectReturnsOnly(JavaProject targetProject, CFG cfg, Set<String> names) {
        for (CFGNode node : cfg.getNodes()) {
            if (node.isReturn()) {
                collectOutReturns(cfg, node, names, targetProject);
            }
        }
    }

    private void collectOutReturns(CFG cfg, CFGNode node, Set<String> names, JavaProject targetProject) {
        CFGStatement statement = (CFGStatement) node;
        if (!statement.getDefVariables().isEmpty()) {
            JVariableReference var = statement.getDefVariables().get(0);
            String qname = var.getQualifiedName().fqn();
            if (!names.contains(qname)) {
                names.add(qname);

                String type = var.getType();
                boolean primitive = var.isPrimitiveType();
                int modifiers = var.getModifiers();
                int index = qname.indexOf(QualifiedName.QualifiedNameSeparator);
                int index2 = qname.indexOf('(');
                String accessName = "$" + qname.substring(index + 1, index2);
                ProjectVariable tvar = new ProjectVariable(cfg, statement, var,
                        Sort.RETURN, Direction.OUT, qname, type, primitive, modifiers, accessName);

                outVariables.add(tvar);
            }

        }
    }


    public List<ProjectVariable> getInVariables() {
        return new ArrayList<>(inVariables);
    }

    public List<ProjectVariable> getOutVariables() {
        return new ArrayList<>(outVariables);
    }

}