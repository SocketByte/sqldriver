package pl.socketbyte.sqldriver.reflect;

import java.lang.reflect.Field;

public interface FieldOperations {
    <T> Object getField(Class<? extends T> clazz, T instance, String fieldName);
    <T> void setField(Class<? extends T> clazz, T instance, String fieldName, Object value);
    Field[] getFields(Class<?> clazz);
    void register(Class<?> clazz);
}
