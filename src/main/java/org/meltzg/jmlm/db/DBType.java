package org.meltzg.jmlm.db;

public enum DBType {
    ARRAY("array"),
    BIGINT("bigint"),
    BOOLEAN("boolean"),
    DOUBLE("double"),
    INT("integer"),
    UUID("uuid"),
    TEXT("varchar");

    private final String name;

    DBType(String n) {
        name = n;
    }

    public boolean equalsName(String otherName) {
        return name.equals(otherName);
    }

    public String toString() {
        return this.name;
    }
}
