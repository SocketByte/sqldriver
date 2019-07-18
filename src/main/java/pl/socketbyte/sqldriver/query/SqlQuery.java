package pl.socketbyte.sqldriver.query;

/**
 * SqlQuery helper class for building simple
 * SQL queries very easily and syntax error-proof
 */
public class SqlQuery {

    /**
     * The built SQL query that will be returned
     */
    private final StringBuilder query;

    private static final StringBuilder builder = new StringBuilder();

    public SqlQuery() {
        this.query = new StringBuilder();
    }

    private SqlQuery(StringBuilder query) {
        this.query = query;
    }

    public SqlQuery table(String tableName) {
        return addToQuery(tableName);
    }

    public SqlQuery createTable(String name) {
        return addToQuery("CREATE TABLE IF NOT EXISTS " + name + " (");
    }

    public SqlQuery record(String name, String type, boolean nullable) {
        return addToQuery("," + name + " " + type + (nullable ? "" : " NOT NULL"));
    }

    public SqlQuery record(String name, String type, boolean nullable, boolean autoincrement) {
        return addToQuery("," + name + " " + type + (nullable ? "" : " NOT NULL")
                + (autoincrement ? " AUTO_INCREMENT" : ""));
    }

    public SqlQuery record(String name, SqlDataType type, boolean nullable) {
        return addToQuery("," + name + " " + type.real() + (nullable ? "" : " NOT NULL"));
    }

    public SqlQuery record(String name, SqlDataType type, boolean nullable, boolean autoincrement) {
        return addToQuery("," + name + " " + type.real() + (nullable ? "" : " NOT NULL")
                + (autoincrement ? " AUTO_INCREMENT" : ""));
    }

    public SqlQuery primaryKey(String key) {
        return addToQuery(",PRIMARY_KEY(" + key + ")");
    }

    public SqlQuery select(String... values) {
        builder.setLength(0);

        for (String value : values) {
            builder.append(", ").append(value);
        }

        if (values.length == 0) {
            builder.append("*");
        }

        return addToQuery("SELECT " +
                builder.toString().replaceFirst(",", "") + " FROM ");
    }

    public SqlQuery update() {
        return addToQuery("UPDATE ");
    }

    public SqlQuery insertInto() {
        return addToQuery("INSERT INTO ");
    }

    public SqlQuery deleteFrom() {
        return addToQuery("DELETE FROM ");
    }

    public SqlQuery where(String... conditions) {
        builder.setLength(0);

        for (String condition : conditions) {
            builder.append(",").append(condition).append("=?");
        }

        return addToQuery(" WHERE " +
                builder.toString().replaceFirst(",", ""));
    }

    public SqlQuery values(int size) {
        builder.setLength(0);
        //size--;

        for (int i = 0; i < size; i++) {
            builder.append(", ?");
        }

        return addToQuery(" VALUES (" +
                builder.toString().replaceFirst(", ", "") + ") ");
    }

    public SqlQuery set(String... conditions) {
        builder.setLength(0);

        for (String condition : conditions) {
            builder.append(",").append(condition).append("=?");
        }

        return addToQuery(" SET (" +
                builder.toString().replaceFirst(",", "") + ")");
    }

    public SqlQuery drop() {
        return addToQuery("DROP TABLE ");
    }

    public String done() {
        if (this.query.toString().contains("CREATE TABLE")) {
            return this.query.toString().replaceFirst("\\(,", "\\(") + ")";
        }
        return this.query.toString();
    }

    private SqlQuery addToQuery(String text) {
        return new SqlQuery(this.query.append(text));
    }
}
