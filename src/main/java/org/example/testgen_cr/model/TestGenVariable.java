package org.example.testgen_cr.model;

public class TestGenVariable {
    public enum Sort {
        FIELD, PARAMETER, RETURN, FFIELD, RFIELD, FMETHOD, RMETHOD
    }

    public enum Direction {
        IN, OUT
    }

    private final Sort sort;
    private final Direction direction;
    private final String qualifiedName;
    private final String type;
    private final boolean isPrimitive;
    private final int modifiers;
    private final String accessName;
    private final int index;

    // コンストラクタ
    public TestGenVariable(Sort sort, Direction direction, String qualifiedName, String type,
                           boolean isPrimitive, int modifiers, String accessName, int index) {
        this.sort = sort;
        this.direction = direction;
        this.qualifiedName = qualifiedName;
        this.type = type;
        this.isPrimitive = isPrimitive;
        this.modifiers = modifiers;
        this.accessName = accessName;
        this.index = index;
    }

    // ゲッター
    public Sort getSort() {
        return sort;
    }

    public Direction getDirection() {
        return direction;
    }

    public String getQualifiedName() {
        return qualifiedName;
    }

    public String getType() {
        return type;
    }

    public boolean isPrimitive() {
        return isPrimitive;
    }

    public int getModifiers() {
        return modifiers;
    }

    public String getAccessName() {
        return accessName;
    }

    public int getIndex() {
        return index;
    }

    // 必要に応じてsetterや他のメソッドを追加
}
