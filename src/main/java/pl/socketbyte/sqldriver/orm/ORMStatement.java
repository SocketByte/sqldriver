package pl.socketbyte.sqldriver.orm;

import pl.socketbyte.sqldriver.SqlConnection;
import pl.socketbyte.sqldriver.SqlDriver;
import pl.socketbyte.sqldriver.orm.annotation.*;
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

    private ORMFieldData getORMDataFromRecord(String recordName) {
        for (Map.Entry<String, ORMFieldData> entry : fieldData.entrySet()) {
            if (entry.getValue().name.equals(recordName)) {
                return entry.getValue();
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
            throw new RuntimeException("Unable to create a table", e);
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
            throw new RuntimeException("Unable to insert an object", e);
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
                        ORMFieldData fieldData = getORMDataFromRecord(rs.getMetaData().getColumnName(i));

                        Object resultValue = value;
                        try {
                            if (value != null)
                                resultValue = UUID.fromString(value.toString());
                        } catch (Exception ignored) {
                        }

                        try {
                            if (value != null) {
                                String potentialBase64 = value.toString();

                                resultValue = ORMSerializer.deserialize(potentialBase64, fieldData.isUsingBukkitSerialization());
                            }
                        } catch (Exception ignored) {
                        }

                        this.operations.setField(this.clazz, object, name, resultValue);
                    }

                    selected.add(object);
                }
            } catch (IllegalAccessException | InstantiationException e) {
                throw new RuntimeException("Unable to read the ResultSet", e);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Unable to select the objects", e);
        }

        return selected;
    }

    public void drop() {
        String query = new SqlQuery()
                .drop().table(this.tableName).done();

        try (PreparedStatement statement = this.connection.createStatement(query)) {
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Unable to drop the table", e);
        }
    }

    public void delete(T instance, Where... whereConditions) {
        SqlQuery queryBuilder = new SqlQuery()
                .deleteFrom()
                .table(this.tableName);

        executeWhereBasedStatement(instance, queryBuilder, whereConditions);
    }

    public void update(T instance, Where... whereConditions) {
        Collection<ORMFieldData> fieldDatas = getFieldData().values();
        String[] fieldNames = new String[fieldDatas.size()];
        int x = 0;
        for (ORMFieldData data : fieldDatas) {
            fieldNames[x] = data.getName();
            x++;
        }
        SqlQuery queryBuilder = new SqlQuery()
                .update()
                .table(this.tableName)
                .set(fieldNames);

        executeWhereBasedStatement(instance, queryBuilder, whereConditions);
    }

    private void executeWhereBasedStatement(T instance, SqlQuery queryBuilder, Where... whereConditions) {
        Tuple<String, List<Where>> tuple = prepareWhereCondition(instance, queryBuilder, whereConditions);
        String query = tuple.getKey();
        List<Where> wheres = tuple.getValue();

        try (PreparedStatement statement = this.connection.createStatement(query)) {
            int index = setStatementArguments(statement, instance);
            for (Where where : wheres) {
                statement.setObject(index, where.value);

                index++;
            }
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Unable to execute statement", e);
        }
    }

    private Tuple<String, List<Where>> prepareWhereCondition(T instance, SqlQuery queryBuilder, Where... original) {
        String query = null;
        List<Where> wheres = new ArrayList<>();
        if (original.length > 0) {
            String[] recordNames = new String[original.length];
            for (int i = 0; i < original.length; i++) {
                recordNames[i] = original[i].recordName;
                wheres.add(original[i]);
            }

            query = queryBuilder.where(recordNames).done();
        }
        else {
            Field[] fields = this.operations.getFields(instance.getClass());
            List<String> recordNames = new ArrayList<>();
            for (Field field : fields) {
                if (!field.isAnnotationPresent(SqlPrimary.class))
                    continue;

                ORMFieldData data = getFieldData().get(field.getName());
                Object value = this.operations.getField(instance.getClass(), instance, field.getName());

                recordNames.add(data.getName());
                wheres.add(new Where(data.getName(), value));

            }
            query = queryBuilder.where(Arrays.toString(recordNames.toArray())).done();
        }
        return new Tuple<>(query, wheres);
    }

    private int setStatementArguments(PreparedStatement statement, T instance) {
        int index = 1;
        for (Map.Entry<String, ORMFieldData> entry : fieldData.entrySet()) {
            String field = entry.getKey();
            ORMFieldData data = entry.getValue();
            try {
                Object object = this.operations.getField(this.clazz, instance, field);

                if (data.getDataType() == SqlDataType.BASE64) {
                    object = ORMSerializer.serialize(object, data.isUsingBukkitSerialization());
                }
                else if (data.getDataType() == SqlDataType.UNIQUE_ID) {
                    object = object.toString();
                }

                statement.setObject(index, object);
            } catch (SQLException e) {
                throw new RuntimeException("Unable to set object data", e);
            }
            index++;
        }
        return index;
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
            boolean useBukkitSerialization = false;

            if (field.isAnnotationPresent(SqlUseBukkitSerialization.class)) {
                useBukkitSerialization = true;
            }

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

            this.fieldData.put(field.getName(), new ORMFieldData(fieldName, fieldType, nullable, useBukkitSerialization));
        }
    }

    private class Tuple<K, V> {
        private K key;
        private V value;

        public Tuple(K key, V value) {
            this.key = key;
            this.value = value;
        }

        public K getKey() {
            return key;
        }

        public void setKey(K key) {
            this.key = key;
        }

        public V getValue() {
            return value;
        }

        public void setValue(V value) {
            this.value = value;
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
        private final boolean useBukkitSerialization;

        public ORMFieldData(String name, SqlDataType dataType, boolean nullable, boolean useBukkitSerialization) {
            this.name = name;
            this.dataType = dataType;
            this.nullable = nullable;
            this.useBukkitSerialization = useBukkitSerialization;
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

        public boolean isUsingBukkitSerialization() {
            return useBukkitSerialization;
        }
    }

}
