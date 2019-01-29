package org.meltzg.jmlm.db;

import java.nio.file.Paths;
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

public abstract class AbstractDBService {
    /**
     * For all of the DBServiceBase methods to work, the table must have a UUID crossDeviceId column
     */
    protected static final ColumnDefinition ID;
    private static final String CONFIG_DIRECTORY;
    private static final String DB_NAME = "jmlm";

    static {
        var dir = System.getenv("JMLM_CONFIG_HOME");
        CONFIG_DIRECTORY = Objects.requireNonNullElseGet(dir, () -> System.getProperty("user.home") + "/.config/jmlm");

        ID = new ColumnDefinition("id", DBType.UUID, UniqueType.PRIMARY_KEY);
    }

    public AbstractDBService() throws SQLException, ClassNotFoundException {
        var conn = getConnection();
        var statement = conn.createStatement();
        var columnDefinitions = getColumnDefinitions().stream()
                .map(ColumnDefinition::toSQLColumnDefinition)
                .collect(Collectors.toList());
        var createTable = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME() + " (" + String.join(", ", columnDefinitions) + ")";
        statement.executeUpdate(createTable);
        conn.close();
    }

    public void dropTable() throws SQLException, ClassNotFoundException {
        try (var conn = getConnection()) {
            executeUpdate(String.format("DROP TABLE %s;", TABLE_NAME()), new ArrayList<>(), conn);
        }
    }

    /**
     * @return The table name for this database service
     */
    public abstract String TABLE_NAME();

    /**
     * @return A list of all column definitions for the table for this service
     */
    protected List<ColumnDefinition> getColumnDefinitions() {
        return new ArrayList<>(Collections.singletonList(ID));
    }

    /**
     * @return a connection to this service's database
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    protected Connection getConnection() throws ClassNotFoundException, SQLException {
        var fullUrl = "jdbc:h2:" + Paths.get(CONFIG_DIRECTORY, DB_NAME);
        Class.forName("org.h2.Driver");

        return DriverManager.getConnection(fullUrl, "", "");
    }

    /**
     * Executes a query (SELECT) on this service's table
     *
     * @param query  - string query to use for creating a PreparedStatement
     * @param params - query parameters to be inserted into the query
     *               The parameters are inserted in the query in order
     * @return Query's ResultSet
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    protected ResultSet executeQuery(String query, List<StatementParameter> params, Connection conn)
            throws SQLException, ClassNotFoundException {
        return (ResultSet) executeQuery(query, params, false, conn);
    }

    /**
     * Executes an update query (INSERT, UPDATE, DELETE) on this service's table
     *
     * @param query
     * @param params
     * @return
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    protected int executeUpdate(String query, List<StatementParameter> params, Connection conn)
            throws SQLException, ClassNotFoundException {
        return (Integer) executeQuery(query, params, true, conn);
    }

    /**
     * Deletes a row by its ID
     *
     * @param id - ID of the row to delete
     * @return number of affected rows (should be 1)
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    protected int deleteById(UUID id, Connection conn) throws ClassNotFoundException, SQLException {
        var params = new ArrayList<StatementParameter>();
        params.add(new StatementParameter(id, DBType.UUID));
        return executeUpdate("DELETE FROM " + TABLE_NAME() + " WHERE " + ID.getName() + " = ?;", params, conn);
    }

    /**
     * Executes a query on this service's table
     *
     * @param query    - query to execute
     * @param params   - parameters to insert into query
     * @param isUpdate - whether this is a selection or an update
     * @return Integer if isUpdate else ResultSet
     * @throws SQLException
     */
    private Object executeQuery(String query, List<StatementParameter> params, boolean isUpdate, Connection conn)
            throws SQLException {
        Object results;

        var stmt = conn.prepareStatement(query);
        setStatementParams(stmt, params, conn);
        if (isUpdate) {
            results = stmt.executeUpdate();
        } else {
            results = stmt.executeQuery();
        }

        return results;
    }

    /**
     * Sets the PreParedStatement stmt's fields to the values in params
     *
     * @param stmt   - PreparedStatement without its fields replaced with values
     * @param params - values to insert into the PreparedStatement
     * @param conn   - database connection
     * @throws SQLException
     */
    private void setStatementParams(PreparedStatement stmt, List<StatementParameter> params, Connection conn)
            throws SQLException {
        if (params != null) {
            for (int i = 1; i <= params.size(); i++) {
                var prm = params.get(i - 1);
                switch (prm.getType()) {
                    case ARRAY:
                        var tempArray = conn.createArrayOf(prm.getItemsType().toString(),
                                ((List) prm.getValue()).toArray());
                        stmt.setArray(i, tempArray);
                        break;
                    case BIGINT:
                        stmt.setLong(i, (Long) prm.getValue());
                        break;
                    case BOOLEAN:
                        stmt.setBoolean(i, (Boolean) prm.getValue());
                        break;
                    case DOUBLE:
                        stmt.setDouble(i, (Double) prm.getValue());
                        break;
                    case INT:
                        stmt.setInt(i, (Integer) prm.getValue());
                        break;
                    case UUID:
                        stmt.setObject(i, ((UUID) prm.getValue()));
                        break;
                    case TEXT:
                        stmt.setString(i, (String) prm.getValue());
                        break;
                    default:
                        System.err.println("Unsupported Parameter Type: " + prm.getType());
                        break;
                }
            }
        }
    }

    /**
     * Enum represents the SQL data types supported by the DBServiceBase
     */
    protected enum DBType {
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

    protected enum UniqueType {
        NOT, PRIMARY_KEY, UNIQUE;
    }

    protected static class ColumnDefinition {
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

    /**
     * Represents a value to use in a PreparedStatement
     */
    protected class StatementParameter {
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
}
