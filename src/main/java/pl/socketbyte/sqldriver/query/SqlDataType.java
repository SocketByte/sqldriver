package pl.socketbyte.sqldriver.query;

public enum SqlDataType {
    // Numeric
    BIT,
    BYTE("TINYINT"),
    SHORT("SMALLINT"),
    LONG("BIGINT"),
    TINYINT,
    SMALLINT,
    INT,
    MEDIUMINT,
    BIGINT,
    BOOLEAN,
    DECIMAL,
    NUMERIC,
    FLOAT,
    REAL,
    DOUBLE,

    // Date/Time
    DATE,
    TIME,
    DATETIME,
    TIMESTAMP,
    YEAR,

    // String/Char
    CHAR,
    VARCHAR,
    TINYTEXT,
    TEXT,
    MEDIUMTEXT,
    LONGTEXT,
    UNIQUE_ID("CHAR(36)"),
    BASE64("MEDIUMTEXT"),

    // Binary
    BINARY,
    VARBINARY,
    IMAGE,

    // Misc
    TINYBLOB,
    BLOB,
    MEDIUMBLOB,
    LONGBLOB,

    // For ORM use
    @Deprecated
    AUTO_DETECT;

    private String real;

    SqlDataType(String real) {
        this.real = real;
    }

    SqlDataType() {
        this.real = this.toString();
    }

    public String real() {
        return this.real;
    }
}
