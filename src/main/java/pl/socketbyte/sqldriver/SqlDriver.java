package pl.socketbyte.sqldriver;

import pl.socketbyte.sqldriver.orm.ORMSerializer;
import pl.socketbyte.sqldriver.reflect.FieldOperations;
import pl.socketbyte.sqldriver.reflect.ReflectTools;

import java.io.Serializable;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The main heart of SqlDriver API, it combines all functionality
 * and wrappers around all complicated stuff
 */
public class SqlDriver {

    private static final String DEFAULT_PATH = "db.properties";

    /**
     * The Hikari connection pool source manager instance
     */
    private SqlSource source;

    private FieldOperations operations;

    private final List<Class<?>> registeredClasses = new ArrayList<>();

    private SqlDriver(String propertiesPath) {
        this.source = new SqlSource(propertiesPath);
        this.operations = ReflectTools.REGULAR_FIELD_OPERATIONS;
    }

    /**
     * Registering a class to minimize performance hit upon first time ORM uses
     * Can minimize overhead by almost 80% which is very beneficial
     * @param clazz Class to register
     */
    public void register(Class<?> clazz) {
        this.registeredClasses.add(clazz);
        this.operations.register(clazz);

        if (clazz.isAssignableFrom(Serializable.class)) {
            ORMSerializer.registerClass(clazz);
        }
    }

    public List<Class<?>> getRegisteredClasses() {
        return registeredClasses;
    }

    /**
     * If you want to achieve maximum performance out of your ORM operations
     * I suggest turning this feature on. There is one downside of this though.
     * When using fast reflections (reflectasm) you can't use private fields
     * in your ORM pojo's as reflectasm doesn't support them.
     *
     * IMPORTANT: Your ORM pojo class fields MUST be public when using this option.
     */
    public void useFastReflections() {
        this.operations = ReflectTools.IMPROVED_FIELD_OPERATIONS;
    }

    public FieldOperations getOperations() {
        return this.operations;
    }

    /**
     * Borrows the connection from the connection pool
     * It's crucial to close it after use!
     * @return SqlConnection
     */
    public SqlConnection borrow() {
        return new SqlConnection(this, this.source.borrow());
    }

    /**
     * Closes the connection pool, use it only if you want to disconnect
     * from the MySQL server completely
     */
    public void close() {
        this.source.close();
    }

    /**
     * Creates the basic SqlDriver class with default properties file path
     * @return SqlDriver
     */
    public static SqlDriver create() {
        return new SqlDriver(DEFAULT_PATH);
    }

    /**
     * Creates the basic SqlDriver class with custom properties file path
     * @param propertiesPath Path to properties file with SQL credentials, can be classpath or filesystem path
     * @return SqlDriver
     */
    public static SqlDriver create(String propertiesPath) {
        return new SqlDriver(propertiesPath);
    }

}
