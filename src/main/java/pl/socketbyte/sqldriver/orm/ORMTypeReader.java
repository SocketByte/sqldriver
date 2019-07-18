package pl.socketbyte.sqldriver.orm;

import pl.socketbyte.sqldriver.query.SqlDataType;

import java.lang.reflect.Field;
import java.util.UUID;

public class ORMTypeReader {

    public static SqlDataType readFieldType(Field field) {
        Class<?> type = field.getType();

        if (type.isAssignableFrom(int.class)) {
            return SqlDataType.INT;
        } else if (type.isAssignableFrom(boolean.class)) {
            return SqlDataType.BOOLEAN;
        } else if (type.isAssignableFrom(byte.class)) {
            return SqlDataType.BYTE;
        } else if (type.isAssignableFrom(char.class)) {
            return SqlDataType.CHAR;
        } else if (type.isAssignableFrom(short.class)) {
            return SqlDataType.SHORT;
        } else if (type.isAssignableFrom(long.class)) {
            return SqlDataType.LONG;
        } else if (type.isAssignableFrom(float.class)) {
            return SqlDataType.FLOAT;
        } else if (type.isAssignableFrom(double.class)) {
            return SqlDataType.DOUBLE;
        } else if (type.isAssignableFrom(String.class)) {
            return SqlDataType.TEXT;
        } else if (type.isAssignableFrom(UUID.class)) {
            return SqlDataType.UNIQUE_ID;
        }
        return SqlDataType.BASE64;
    }

}
