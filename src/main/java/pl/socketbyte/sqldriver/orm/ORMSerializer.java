package pl.socketbyte.sqldriver.orm;

import org.nustaq.serialization.FSTConfiguration;

import java.io.Serializable;
import java.util.Base64;

public class ORMSerializer {

    private static FSTConfiguration fst = FSTConfiguration.createDefaultConfiguration();

    public static void registerClass(Class<?> clazz) {
        fst.registerClass(clazz);
    }

    public static <T> String serialize(T object) {
        byte[] serialized = fst.asByteArray(object);

        return Base64.getEncoder().encodeToString(serialized);
    }

    public static Object deserialize(String base64) {
        return fst.asObject(Base64.getDecoder().decode(base64));
    }

}
