package pl.socketbyte.sqldriver.orm;

import pl.socketbyte.sqldriver.SqlConnection;
import pl.socketbyte.sqldriver.SqlDriver;
import pl.socketbyte.sqldriver.orm.annotation.SqlField;
import pl.socketbyte.sqldriver.orm.annotation.SqlNullable;
import pl.socketbyte.sqldriver.orm.annotation.SqlObject;
import pl.socketbyte.sqldriver.orm.annotation.SqlTransient;
import pl.socketbyte.sqldriver.query.SqlQuery;
import pl.socketbyte.sqldriver.query.SqlDataType;
import pl.socketbyte.sqldriver.reflect.FieldOperations;

import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class ORMStatement<T> {
    private final FieldOperations operations;

    private final SqlConnection connection;
    private final Class<? extends T> clazz;

    private final Map<String, ORMFieldData> fieldData = new LinkedHashMap<>();

    private String tableName;

    protected Map<String, ORMFieldData> getFieldData() {
        return this.fieldData;
    }

    private String getOriginalFieldName(String recordName) {
        for (Map.Entry<String, ORMFieldData> entry : fieldData.entrySet()) {
            if (entry.getValue().name.equals(recordName)) {
                return entry.getKey();
            }
        }
        return null;
    }

    public ORMStatement(SqlDriver driver, SqlConnection connection, Class<? extends T> clazz) {
        this.connection = connection;
        this.clazz = clazz;

        this.operations = driver.getOperations();

        this.fetchClassData();
    }

    public void createTable() {
        SqlQuery queryBuilder = new SqlQuery()
                .createTable(this.tableName);
        for (Map.Entry<String, ORMFieldData> entry : fieldData.entrySet()) {
            ORMFieldData data = entry.getValue();

            queryBuilder = queryBuilder.record(data.getName(), data.getDataType(), data.isNullable());
        }
        String query = queryBuilder.done();

        try (PreparedStatement statement = this.connection.createStatement(query)) {
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void insert(T instance) {
        String query = new SqlQuery()
                .insertInto()
                .table(this.tableName)
                .values(this.fieldData.size())
                .done();
        try (PreparedStatement statement = this.connection.createStatement(query)) {
            setStatementArguments(statement, instance);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<T> select(Where... whereConditions) {
        List<T> selected = new ArrayList<>();

        SqlQuery queryBuilder = new SqlQuery()
                .select()
                .table(this.tableName);

        if (whereConditions.length > 0) {
            String[] recordNames = new String[whereConditions.length];
            for (int i = 0; i < whereConditions.length; i++) {
                recordNames[i] = whereConditions[i].recordName;
            }

            queryBuilder = queryBuilder.where(recordNames);
        }
        String query = queryBuilder.done();

        try (PreparedStatement statement = this.connection.createStatement(query)) {
            int index = 1;
            for (Where where : whereConditions) {
                statement.setObject(index, where.value);

                index++;
            }
            try (ResultSet rs = statement.executeQuery()) {
                int count = rs.getMetaData().getColumnCount();
                while (rs.next()) {
                    T object = this.clazz.newInstance();

                    for (int i = 1; i <= count; i++) {
                        Object value = rs.getObject(i);
                        String name = getOriginalFieldName(rs.getMetaData().getColumnName(i));

                        Object resultValue = value;
                        try {
                            if (value != null)
                                resultValue = UUID.fromString(value.toString());
                        } catch (Exception e) {
                        }

                        this.operations.setField(this.clazz, object, name, resultValue);
                    }

                    selected.add(object);
                }
            } catch (IllegalAccessException | InstantiationException e) {
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return selected;
    }

    private void setStatementArguments(PreparedStatement statement, T instance) {
        int index = 1;
        for (Map.Entry<String, ORMFieldData> entry : fieldData.entrySet()) {
            String field = entry.getKey();
            ORMFieldData data = entry.getValue();
            try {
                Object object = this.operations.getField(this.clazz, instance, field);

                if (data.getDataType() == SqlDataType.BASE64) {
                    object = ORMSerializer.serialize(object);
                }
                else if (data.getDataType() == SqlDataType.UNIQUE_ID) {
                    object = object.toString();
                }

                statement.setObject(index, object);
            } catch (SQLException e) {
                e.printStackTrace();
            }
            index++;
        }
    }

    @SuppressWarnings("deprecation")
    private void fetchClassData() {
        if (!this.clazz.isAnnotationPresent(SqlObject.class)) {
            throw new RuntimeException("ORM class object has no SqlObject annotation");
        }

        SqlObject object = this.clazz.getAnnotation(SqlObject.class);
        this.tableName = object.tableName();

        for (Field field : this.operations.getFields(this.clazz)) {
            if (field.isAnnotationPresent(SqlTransient.class))
                continue;

            String fieldName = null;
            SqlDataType fieldType = null;
            boolean nullable = false;

            if (field.isAnnotationPresent(SqlField.class)) {
                SqlField property = field.getAnnotation(SqlField.class);

                if (!property.name().equals("")) {
                    fieldName = property.name();
                }

                if (property.type() != SqlDataType.AUTO_DETECT) {
                    fieldType = property.type();
                }
            }

            if (field.isAnnotationPresent(SqlNullable.class)) {
                nullable = true;
            }

            if (fieldName == null)
                fieldName = field.getName().toLowerCase();

            if (fieldType == null)
                fieldType = ORMTypeReader.readFieldType(field);

            this.fieldData.put(field.getName(), new ORMFieldData(fieldName, fieldType, nullable));
        }
    }

    public class Where<V> {
        private final String recordName;
        private final V value;

        public Where(String recordName, V value) {
            this.recordName = recordName;
            this.value = value;
        }

        public String getRecordName() {
            return this.recordName;
        }

        public V getValue() {
            return this.value;
        }
    }

    private class ORMFieldData {
        private final String name;
        private final SqlDataType dataType;
        private final boolean nullable;

        public ORMFieldData(String name, SqlDataType dataType, boolean nullable) {
            this.name = name;
            this.dataType = dataType;
            this.nullable = nullable;
        }

        public String getName() {
            return name;
        }

        public SqlDataType getDataType() {
            return dataType;
        }

        public boolean isNullable() {
            return nullable;
        }
    }

}
