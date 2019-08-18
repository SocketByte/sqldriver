package pl.socketbyte.sqldriver.orm;

import org.bukkit.util.io.BukkitObjectOutputStream;
import org.nustaq.serialization.FSTConfiguration;
import pl.socketbyte.sqldriver.orm.annotation.SqlUseBukkitSerialization;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.util.Base64;

public class ORMSerializer {

    private static FSTConfiguration fst = FSTConfiguration.createDefaultConfiguration();

    public static void registerClass(Class<?> clazz) {
        fst.registerClass(clazz);
    }

    public static <T> String serialize(T object, boolean useBukkitSerialization) {
        byte[] serialized;
        if (useBukkitSerialization) {
            try {
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);

                dataOutput.writeObject(object);
                dataOutput.close();

                serialized = outputStream.toByteArray();
            } catch (Exception e) {
                throw new RuntimeException("Bukkit serialization failed", e);
            }
        }
        else serialized = fst.asByteArray(object);

        return Base64.getEncoder().encodeToString(serialized);
    }

    public static Object deserialize(String base64) {
        return fst.asObject(Base64.getDecoder().decode(base64));
    }

}
