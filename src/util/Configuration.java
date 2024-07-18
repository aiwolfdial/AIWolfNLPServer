package util;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;

import org.ini4j.Ini;
import org.ini4j.Profile.Section;

public class Configuration {
    public static void loadFile(String path, String sectionName, Object configObject)
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
        Configuration.loadSection(section, configObject);
        loadSection(section, configObject);
    }

    public static void loadSection(Section section, Object configObject)
            throws NoSuchFieldException, IllegalAccessException {
        for (Field field : configObject.getClass().getDeclaredFields()) {
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
                field.setInt(configObject, Integer.parseInt(value));
            } else if (field.getType() == long.class) {
                field.setLong(configObject, Long.parseLong(value));
            } else if (field.getType() == common.GameConfiguration.HumanRole.class) {
                field.set(configObject, common.GameConfiguration.HumanRole.valueOf(value));
            } else if (field.getType() == Class.class) {
                try {
                    field.set(configObject, Class.forName(value));
                } catch (ClassNotFoundException e) {
                    throw new IllegalArgumentException("Invalid class name: " + value, e);
                }
            } else if (field.getType() == boolean.class) {
                field.setBoolean(configObject, Boolean.parseBoolean(value));
            } else {
                field.set(configObject, value);
            }
            System.out.println("Loaded: " + key + " = " + value);
        }
    }
}
