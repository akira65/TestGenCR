package org.example.testgen_cr.model_buffs;

import org.jtool.cfg.CFG;
import org.jtool.cfg.CFGNode;
import org.jtool.cfg.JVariableReference;
import org.jtool.srcmodel.JavaMethod;

import java.util.*;

public class TestGenVariable {
    private final static String simpleType[] = {
            "java.lang.String",
            "java.lang.Byte",
            "java.lang.Short",
            "java.lang.Integer",
            "java.lang.Long",
            "java.lang.Float",
            "java.lang.Double"
    };

    public enum Sort {
        FIELD, PARAMETER, RETURN, FFIELD, RFIELD, FMETHOD, RMETHOD,
    }

    public enum Direction {
        IN, OUT,
    }

    private CFG cfg;
    private CFGNode cfgNode;
    private JVariableReference variable;

    private Sort sort;
    private Direction dir = null;
    private String qname;
    private String type;
    private boolean isPrimitiveType;
    protected int modifiers;
    private String access;
    private int index;

    private TestGenVariable parent = null;
    private Set<TestGenVariable> enclosingVariables = new HashSet<>();

    private JavaMethod accessor;

    public TestGenVariable(CFG cfg, CFGNode node, JVariableReference var,
                           Sort sort, Direction dir, String qname, String type,
                           boolean primitive, int modifiers, String access) {
        this(cfg, node, var, sort, dir, qname, type, primitive, modifiers, access, -1);
    }

    public TestGenVariable(CFG cfg, CFGNode node, JVariableReference var,
                           Sort sort, Direction dir, String qname, String type,
                           boolean primitive, int modifiers, String access, int index) {
        this.cfg = cfg;
        this.cfgNode = node;
        this.variable = var;

        this.sort = sort;
        this.dir = dir;
        this.qname = qname;
        this.type = type;
        this.isPrimitiveType = primitive;
        this.modifiers = modifiers;
        this.access = access;
        this.index = index;
    }

    public boolean isParameter() {
        return sort == Sort.PARAMETER;
    }

    public String getQualifiedName() {
        if (isParameter()) {
            int index = qname.lastIndexOf('$');
            return qname.substring(0, index);
        } else {
            return qname;
        }
    }

    public static void sort(List<? extends TestGenVariable> vars) {
        Collections.sort(vars, new Comparator<TestGenVariable>() {

            @Override
            public int compare(TestGenVariable v1, TestGenVariable v2) {
                return v1.getQualifiedName().compareTo(v2.getQualifiedName());
            }
        });
    }
    
}
