package pl.socketbyte.sqldriver.reflect;

import com.esotericsoftware.reflectasm.FieldAccess;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class ImprovedFieldOperations implements FieldOperations {

    private final Map<Class<?>, FieldAccess> cachedFieldAccessors = new HashMap<>();

    public FieldAccess getFieldAccess(Class<?> clazz) {
        FieldAccess access = cachedFieldAccessors.get(clazz);
        if (access != null)
            return access;

        access = FieldAccess.get(clazz);
        cachedFieldAccessors.put(clazz, access);

        return access;
    }

    @Override
    public <T> Object getField(Class<? extends T> clazz,
                                      T instance, String fieldName) {
        FieldAccess access = getFieldAccess(clazz);

        return access.get(instance, fieldName);
    }

    @Override
    public <T> void setField(Class<? extends T> clazz,
                                    T instance, String fieldName, Object value) {
        FieldAccess access = getFieldAccess(clazz);

        access.set(instance, fieldName, value);
    }

    @Override
    public Field[] getFields(Class<?> clazz) {
        FieldAccess access = getFieldAccess(clazz);

        return access.getFields();
    }

    @Override
    public void register(Class<?> clazz) {
        getFieldAccess(clazz);
    }

}
