package org.example.testgen_cr.searcher;

import org.example.testgen_cr.model.TestGenMethod;
import org.example.testgen_cr.model.TestGenMethodSeq;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Filter {
    private final static String[] simpleType = {
            "java.lang.String",
            "byte",
            "short",
            "int",
            "long",
            "float",
            "boolean",
            "char",
    };

    private List<TestGenMethodSeq> filteredSeqList = new ArrayList<TestGenMethodSeq>();

    public Filter(List<TestGenMethodSeq> seqList) {
        for (TestGenMethodSeq seq : seqList) {
            for (TestGenMethod aaa : seq.getMethodChain()) {
                filteringMethodSeq(aaa);
            }
        }
    }

    private TestGenMethod filteringMethodSeq(TestGenMethod targetMethod) {
//        TestGenMethod targetMethod = seq.getSeqMethod(0);
        for (String variableType : targetMethod.getFullVariableTypeList()) {
            System.out.println("variableType");
            System.out.println(variableType);
            if (variableType.contains(",")) {
                System.out.println("contains comma");
                System.out.println(variableType);

            }
            ArrayList<String> noGenericSymbolTypeList = removeGenericSymbol(new ArrayList<String>(Collections.singletonList(variableType)));
            ArrayList<String> noArraySymbolTypeList = removeArraySymbol(noGenericSymbolTypeList);

            System.out.println(noArraySymbolTypeList);
            for (String aa : noArraySymbolTypeList) {
                if (aa.contains("?")) {
                    System.out.println("sdlkfjaflkjaf");
                    System.out.println(targetMethod.getFullVariableTypeList());
                }
            }
        }
        return targetMethod;
    }

    private ArrayList<String> removeArraySymbol(ArrayList<String> typeList) {
        for (String type : typeList) {
            if (type.endsWith("[]")) {
                typeList.remove(type);
                typeList.add(type.substring(0, type.length() - 2));
            }
        }
        return typeList;
    }

    private ArrayList<String> removeGenericSymbol(ArrayList<String> simpleTypeList) {
        for (String simpleType : simpleTypeList) {
            System.out.println("simpleType");
            int genericStart = simpleType.indexOf('<');
            if (genericStart != -1) {
                int genericEnd = simpleType.indexOf('>');
                if (genericEnd != -1) {
                    simpleTypeList.remove(simpleType);
                    String genericContent = simpleType.substring(genericStart + 1, genericEnd).trim();
                    System.out.println(simpleType);
                    System.out.println("genericContent");
                    System.out.println(Arrays.asList(genericContent.split(",")));

                    simpleTypeList.addAll(Arrays.asList(genericContent.split(",")));
                    removeGenericSymbol(simpleTypeList);
                }
            }
        }
        return simpleTypeList;
    }
}
