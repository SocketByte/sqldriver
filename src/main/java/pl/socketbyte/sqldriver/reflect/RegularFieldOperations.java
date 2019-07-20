package pl.socketbyte.sqldriver.reflect;

import java.lang.reflect.Field;

public class RegularFieldOperations implements FieldOperations {

    @Override
    public <T> Object getField(Class<? extends T> clazz,
                               T instance, String fieldName) {
        try {
            Field field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);

            return field.get(instance);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Unable to read the field value", e);
        }
    }

    @Override
    public <T> void setField(Class<? extends T> clazz,
                             T instance, String fieldName, Object value) {
        try {
            Field field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);

            field.set(instance, value);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Unable to set the field value", e);
        }
    }

    @Override
    public Field[] getFields(Class<?> clazz) {
        return clazz.getFields();
    }

    @Override
    public void register(Class<?> clazz) {

    }
}
