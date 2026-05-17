package ru.mephi.sirnya.util;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;

public class JsonUtil {
    private static final ObjectMapper mapper = new ObjectMapper();

    public static <T> T parse(InputStream is, Class<T> clazz) throws IOException {
        return mapper.readValue(is, clazz);
    }

    public static String toJson(Object obj) throws IOException {
        return mapper.writeValueAsString(obj);
    }
}