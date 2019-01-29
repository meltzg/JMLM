package org.meltzg.jmlm.db;

public class ColumnDefinition {
    private String name;
    private DBType type;
    private UniqueType uniqueType;

    public ColumnDefinition(String name, DBType type) {
        this(name, type, UniqueType.NOT);
    }

    public ColumnDefinition(String name, DBType type, UniqueType uniqueType) {
        this.name = name;
        this.type = type;
        this.uniqueType = uniqueType;
    }

    public String getName() {
        return name;
    }

    public DBType getType() {
        return type;
    }

    public UniqueType getUniqueType() {
        return uniqueType;
    }

    public String toSQLColumnDefinition() {
        var def = String.join(" ", name, type.toString());
        switch (uniqueType) {

            case NOT:
                break;
            case PRIMARY_KEY:
                def = String.join(" ", def, "PRIMARY KEY");
                break;
            case UNIQUE:
                def = String.join(" ", def, "UNIQUE");
                break;
        }
        return def;
    }
}
