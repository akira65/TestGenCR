package org.example.testgen_cr.results;

import org.example.testgen_cr.model_buffs.TestGenVariable;
import org.example.testgen_cr.model_buffs.TestGenVariable.Sort;

public class ResultVariable {
    private String label;

    private TestGenVariable.Sort sort;
    private TestGenVariable.Direction dir;
    private String type;
    private String name;
    private String value;

    public ResultVariable(String label, String value) {
        this.label = label;
        this.sort = getSort(label);
        this.dir = getDirection(label);
        this.type = getType(label);
        this.name = getName(label);
        this.value = getValue(value, type);
    }

    private TestGenVariable.Sort getSort(String label) {
        String sort = getSortLabel(label);
        if (TestGenVariable.Sort.FIELD.toString().startsWith(sort)) {
            return Sort.FIELD;
        } else if (Sort.PARAMETER.toString().startsWith(sort)) {
            return Sort.PARAMETER;
        } else if (Sort.RETURN.toString().startsWith(sort)) {
            return Sort.RETURN;
        } else if (Sort.FFIELD.toString().startsWith(sort)) {
            return Sort.FFIELD;
        } else if (Sort.RFIELD.toString().startsWith(sort)) {
            return Sort.RFIELD;
        } else if (Sort.FMETHOD.toString().startsWith(sort)) {
            return Sort.FMETHOD;
        } else if (Sort.RMETHOD.toString().startsWith(sort)) {
            return Sort.RMETHOD;
        }
        return null;
    }

    private TestGenVariable.Direction getDirection(String label) {
        String inout = getInOutLabel(label);
        if (TestGenVariable.Direction.IN.toString().startsWith(inout)) {
            return TestGenVariable.Direction.IN;
        } else if (TestGenVariable.Direction.OUT.toString().startsWith(inout)) {
            return TestGenVariable.Direction.OUT;
        }
        return null;
    }

    private String getSortLabel(String label) {
        return label.substring(0, 2).toUpperCase();
    }

    private String getInOutLabel(String label) {
        return label.substring(3, 4).toUpperCase();
    }

    private String getType(String label) {
        int index = label.indexOf('@');
        int index2 = label.indexOf('%');
        return label.substring(index + 1, index2);
    }

    private String getValue(String value, String type) {
        if (type.equals("java.lang.String")) {
            return restoreEscapedString(value);
        } else {
            return value;
        }
    }

    static String restoreEscapedString(String str) {
        return convertToUnicodeEscape(str.replaceAll("\\\\\"", "\"").replaceAll("\\\\\'", "\'"));
    }

    private static String convertToUnicodeEscape(String str) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            char ch = str.charAt(i);
            if (Character.isSurrogate(ch)) {
                sb.append(String.format("\\u%04X", (int) ch));
            } else {
                sb.append(ch);
            }
        }
        return sb.toString();
    }

    private String getName(String label) {
        int index = label.indexOf('%');
        return label.substring(index + 1);
    }
}
