package pl.socketbyte.sqldriver;

import pl.socketbyte.sqldriver.orm.ORMStatement;
import pl.socketbyte.sqldriver.orm.annotation.SqlObject;
import pl.socketbyte.sqldriver.orm.annotation.SqlReference;
import pl.socketbyte.sqldriver.query.SqlQuery;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

/**
 * Simple wrapper around Java's Connection
 * Provides mixed functionality, with SqlDriver-only features
 * and Java's Connection features
 *
 * Mainly created to abstract the SqlDriver API from Java's and HikariCP APIs
 */
@SuppressWarnings("unchecked")
public class SqlConnection implements AutoCloseable {

    /**
     * Java's connection instance borrowed from the connection pool, must be closed after use
     */
    private final Connection connection;
    private final SqlDriver driver;

    private final Map<Class<?>, ORMStatement> cachedStatements = new HashMap<>();

    protected SqlConnection(SqlDriver driver, Connection connection) {
        this.driver = driver;
        this.connection = connection;

        this.cacheStatements(driver.getRegisteredClasses());
    }

    private void cacheStatements(List<Class<?>> classes) {
        for (Class<?> clazz : classes) {
            makeORMStatement(clazz);
        }
    }

    /**
     * Loads all the ORM classes simultenously
     * This is very important when using SqlReference annotations
     * It is recommended to use this method over all other selection methods
     *
     * TODO this code is a mess, fix it
     */
    public <T> Map<Class<? extends T>, SelectionResult> selectAll(Class<? extends T>... classes) {
        Map<Class<? extends T>, SelectionResult> results = new LinkedHashMap<>();
        for (Class clazz : classes) {
            if (!clazz.isAnnotationPresent(SqlObject.class))
                continue;

            SqlObject object = (SqlObject) clazz.getAnnotation(SqlObject.class);

            ORMStatement statement = makeORMStatement(clazz);
            List list = statement.select();

            results.put(clazz, new SelectionResult(object.tableName(), list));
        }

        for (Map.Entry<Class<? extends T>, SelectionResult> entry : results.entrySet()) {
            Class<?> resultClass = entry.getClass();
            SelectionResult result = entry.getValue();
            List objects = result.getObjects();

            for (Object object : objects) {
                Field[] fields = this.driver.getOperations().getFields(object.getClass());

                for (Field field : fields) {
                    if (!field.isAnnotationPresent(SqlReference.class))
                        continue;

                    SqlReference reference = field.getAnnotation(SqlReference.class);
                    String[] rules = reference.rule();

                    Class<?> fieldType = reference.reference();
                    String fieldName = field.getName();

                    List referencedObjects = new ArrayList();
                    SelectionResult referencedResult = results.get(fieldType);

                    if (referencedResult == null)
                        throw new RuntimeException("The referenced classes were not loaded successfully");

                    List<Object> matchingObjects = new ArrayList<>();
                    for (String rule : rules) {
                        String[] split = rule.split("=");

                        String compareWhat = split[1];
                        String compareTo = split[0];

                        boolean compareByValues = false;
                        Object valueCompareWhat = null;
                        try {
                            valueCompareWhat = object.getClass().getField(compareWhat).get(object);
                        } catch (IllegalAccessException | NoSuchFieldException e) {
                            valueCompareWhat = compareWhat;
                            compareByValues = true;
                        }
                        for (Object referencedObject : referencedResult.getObjects()) {
                            Object valueCompareTo = null;
                            try {
                                valueCompareTo = referencedObject.getClass().getField(compareTo).get(referencedObject);
                            } catch (IllegalAccessException | NoSuchFieldException e) {
                                continue;
                            }

                            if (compareByValues) {
                                if (valueCompareWhat.toString().equals(valueCompareTo.toString())) {
                                    matchingObjects.add(referencedObject);
                                }
                                else {
                                    matchingObjects.remove(referencedObject);
                                    break;
                                }
                            }
                            else {
                                if (valueCompareWhat.equals(valueCompareTo)) {
                                    matchingObjects.add(referencedObject);
                                }
                                else {
                                    matchingObjects.remove(referencedObject);
                                    break;
                                }
                            }
                        }
                    }

                    if (field.getType().isAssignableFrom(List.class)) {
                        try {
                            field.set(object, matchingObjects);
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                            throw new RuntimeException("Could not set the matching objects to the SqlReference field");
                        }
                    }
                    else {
                        try {
                            if (matchingObjects.size() <= 0)
                                continue;
                            field.set(object, matchingObjects.get(0));
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                            throw new RuntimeException("Could not set the matching object to the SqlReference field");
                        }
                    }
                }
            }
        }
        return results;
    }

    public <T> void insert(T instance) {
        ORMStatement<T> statement = (ORMStatement<T>) makeORMStatement(instance.getClass());

        statement.insert(instance);
    }

    public <T> void createTable(Class<? extends T> clazz) {
        ORMStatement<T> statement = makeORMStatement(clazz);

        statement.createTable();
    }

    public PreparedStatement createStatement(String query) {
        try {
            return this.connection.prepareStatement(query);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public PreparedStatement createStatement(SqlQuery query) {
        try {
            return this.connection.prepareStatement(query.done());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public <T> ORMStatement<T> makeORMStatement(Class<? extends T> clazz) {
        ORMStatement<T> statement = cachedStatements.get(clazz);
        if (statement != null) {
            return statement;
        }
        else {
            statement = new ORMStatement<>(this.driver, this, clazz);
            cachedStatements.put(clazz, statement);
        }

        return statement;
    }

    /**
     * Method for closing the connection (basically returning it back to the connection poll)
     * Not closing the connection after use WILL lead to severe connection leaks
     */
    @Override
    public void close() {
        try {
            this.connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Connection was not closed successfully, " +
                    "this will lead to connection leaks!");
        }
    }

    /**
     * Getter for raw Java connection, useful for slightly more advanced users or certain scenarios
     * Generally try to avoid using it
     * @return Connection
     */
    public Connection getRawConnection() {
        return this.connection;
    }
}

