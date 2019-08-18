package pl.socketbyte.sqldriver.orm;

import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.nustaq.serialization.FSTConfiguration;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;
import pl.socketbyte.sqldriver.orm.annotation.SqlUseBukkitSerialization;

import java.io.ByteArrayInputStream;
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

    public static Object deserialize(String base64, boolean useBukkitSerialization) {
        if (useBukkitSerialization) {
            try {
                ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(base64));
                BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);

                return dataInput.readObject();
            } catch (Exception e) {
                throw new RuntimeException("Bukkit deserialization failed", e);
            }
        }

        return fst.asObject(Base64.getDecoder().decode(base64));
    }

}
