package org.meltzg.jmlm.db;

public class StatementParameter {
    private Object value;
    private DBType type;
    /**
     * The type of elements if this's type is DBType.ARRAY
     */
    private DBType itemsType;

    public StatementParameter(Object value, DBType type) {
        this(value, type, null);
    }

    public StatementParameter(Object value, DBType type, DBType itemsType) {
        this.value = value;
        this.type = type;
        this.itemsType = itemsType;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public DBType getType() {
        return type;
    }

    public void setType(DBType type) {
        this.type = type;
    }

    public DBType getItemsType() {
        return itemsType;
    }

    public void setItemsType(DBType itemsType) {
        this.itemsType = itemsType;
    }
}
