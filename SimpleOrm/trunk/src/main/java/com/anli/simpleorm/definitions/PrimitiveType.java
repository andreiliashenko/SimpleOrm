package com.anli.simpleorm.definitions;

public enum PrimitiveType {

    STRING(false, true),
    NUMERIC(true, false),
    BOOLEAN(false, false),
    DATE(true, false),
    REFERENCE(false, false),
    OTHER(false, false);

    protected final boolean comparable;
    protected final boolean character;

    private PrimitiveType(boolean comparable, boolean character) {
        this.comparable = comparable;
        this.character = character;
    }

    public boolean isComparable() {
        return comparable;
    }

    public boolean isCharacter() {
        return character;
    }

}
