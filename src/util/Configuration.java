package util;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.ini4j.Ini;
import org.ini4j.Profile.Section;

public class Configuration {
    private static final Logger logger = LogManager.getLogger(Configuration.class);

    public static Object loadFile(String path, String sectionName, Object object)
            throws NoSuchFieldException, IllegalAccessException, IOException {
        File file = new File(path);
        if (!file.exists()) {
            throw new IOException("File not found: " + path);
        }
        Ini ini = new Ini(file);
        Section section = ini.get(sectionName);
        if (section == null) {
            throw new IOException("Section not found: " + sectionName);
        }
        loadSection(section, object);
        logger.info(String.format("Loaded configuration from %s", path));
        return object;
    }

    public static void loadSection(Section section, Object object)
            throws NoSuchFieldException, IllegalAccessException {
        for (Field field : object.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            String key = field.getName();
            if (!section.containsKey(key)) {
                if (key.startsWith("player") && (key.endsWith("Ip") || key.endsWith("Port"))) {
                    continue;
                }
                throw new NoSuchFieldException("Missing key: " + key);
            }
            String value = section.get(key);
            if (field.getType() == int.class) {
                field.setInt(object, Integer.parseInt(value));
            } else if (field.getType() == long.class) {
                field.setLong(object, Long.parseLong(value));
            } else if (field.getType() == common.GameConfiguration.HumanRole.class) {
                field.set(object, common.GameConfiguration.HumanRole.valueOf(value));
            } else if (field.getType() == Class.class) {
                try {
                    field.set(object, Class.forName(value));
                } catch (ClassNotFoundException e) {
                    throw new IllegalArgumentException("Invalid class name: " + value, e);
                }
            } else if (field.getType() == boolean.class) {
                field.setBoolean(object, Boolean.parseBoolean(value));
            } else {
                field.set(object, value);
            }
            logger.debug(String.format("Loaded: %s = %s", key, value));
        }
    }
}
