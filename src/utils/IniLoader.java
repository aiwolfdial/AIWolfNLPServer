package utils;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.stream.Collectors;

import org.ini4j.Ini;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

public class IniLoader {
    private static Map<String, Map<String, String>> parseIniFile(File file)
            throws IOException {
        Ini ini = new Ini(file);
        return ini.entrySet().stream()
                .collect(Collectors.toMap(
                        e -> e.getKey(),
                        e -> e.getValue().entrySet().stream()
                                .collect(Collectors.toMap(
                                        entry -> entry.getKey(),
                                        entry -> entry.getValue()))));
    }

    public static <T> T load(String filename, Class<T> clazz) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        Map<String, Map<String, String>> map = parseIniFile(new File(filename));
        return mapper.convertValue(map.get(clazz.getSimpleName()), clazz);
    }
}
